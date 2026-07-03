import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { z } from "zod";
import { OptionalGuestDetailsSection } from "@/components/customer/OptionalGuestDetailsSection";
import { ErrorState } from "@/components/common/ErrorState";
import { FormErrorText } from "@/components/common/FormErrorText";
import { PageHeader } from "@/components/common/PageHeader";
import { Spinner } from "@/components/common/Spinner";
import { AvailabilityService } from "@/services/AvailabilityService";
import { GuestService } from "@/services/GuestService";
// import { PaymentService } from "@/services/PaymentService";
import { ReservationService } from "@/services/ReservationService";
import { RoomTypeService } from "@/services/RoomTypeService";
import { useAuthStore } from "@/store/authStore";
import type { AvailabilityCustomerResponseDTO, AvailabilityResponseDTO, PaymentMethod } from "@/types/api";
import {
  createEmptyGuestEntry,
  getActiveGuestEntries,
  type OptionalGuestEntry,
  validateOptionalGuestEntries,
} from "@/utils/guest";
import { getApiErrorMessage } from "@/utils/http";
import { primaryButtonClass } from "@/utils/ui";

const schema = z.object({
  roomTypeId: z.string().min(1, "Room type is required."),
  checkInDate: z.string().min(1, "Check-in date is required."),
  checkOutDate: z.string().min(1, "Check-out date is required."),
}).refine((values) => new Date(values.checkOutDate) > new Date(values.checkInDate), {
  message: "Check-out date must be after check-in date.",
  path: ["checkOutDate"],
});

const bookingSchema = z.object({
  guestCount: z.string().min(1, "Guest count is required."),
  specialRequest: z.string(),
  paymentMethod: z.enum(["UPI", "CARD", "NET_BANKING", "WALLET"]),
});

interface FormValues {
  roomTypeId: string;
  checkInDate: string;
  checkOutDate: string;
}

interface BookingFormValues {
  guestCount: string;
  specialRequest: string;
  paymentMethod: PaymentMethod;
}

const paymentMethodOptions: PaymentMethod[] = ["UPI", "CARD", "NET_BANKING", "WALLET"];

export default function SearchRoomsPage() {
  const user = useAuthStore((state) => state.user);
  const [optionalGuests, setOptionalGuests] = useState<OptionalGuestEntry[]>([]);
  const [optionalGuestFormError, setOptionalGuestFormError] = useState<string | null>(null);

  const {
    data: roomTypes = [],
    isLoading: roomTypeLoading,
    isError: roomTypesError,
    refetch: refetchRoomTypes,
  } = useQuery({
    queryKey: ["roomTypes"],
    queryFn: RoomTypeService.getAllRoomTypes,
    retry: false,
  });

  const searchForm = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { roomTypeId: "", checkInDate: "", checkOutDate: "" },
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = searchForm;

  const bookingForm = useForm<BookingFormValues>({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      guestCount: "1",
      specialRequest: "",
      paymentMethod: "UPI",
    },
  });

  const guestCountValue = Number(bookingForm.watch("guestCount") || "1");

  useEffect(() => {
    setOptionalGuests((current) => (current.length > guestCountValue ? current.slice(0, guestCountValue) : current));
  }, [guestCountValue]);

  const availabilityMutation = useMutation({
    mutationFn: async (payload: FormValues) => {
      const request = {
        roomTypeId: Number(payload.roomTypeId),
        checkInDate: payload.checkInDate,
        checkOutDate: payload.checkOutDate,
      };

      const [search, check] = await Promise.all([
        AvailabilityService.searchAvailability(request),
        AvailabilityService.checkAvailability(request),
      ]);
      return { search, check };
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to search availability."));
    },
  });

  const reservationMutation = useMutation({
    mutationFn: async (bookingValues: BookingFormValues) => {
      const searchFormValues = searchForm.getValues();
      const guestValidationError = validateOptionalGuestEntries(optionalGuests, Number(bookingValues.guestCount));

      if (guestValidationError) {
        throw new Error(guestValidationError);
      }

      if (!user) {
        throw new Error("No active user session.");
      }

      const reservation = await ReservationService.createReservation({
        userId: user.userId,
        roomTypeId: Number(searchFormValues.roomTypeId),
        checkInDate: searchFormValues.checkInDate,
        checkOutDate: searchFormValues.checkOutDate,
        guestCount: Number(bookingValues.guestCount),
        specialRequest: bookingValues.specialRequest,
        paymentMethod: bookingValues.paymentMethod,
      });

//        const payment = await PaymentService.createPayment({
//          reservationId: reservation.reservationId,
//          paymentMethod: bookingValues.paymentMethod,
//     });

      const activeGuests = getActiveGuestEntries(optionalGuests);
      const savedGuests = [];

      try {
        for (const guest of activeGuests) {
          const savedGuest = await GuestService.createGuest({
            reservationId: reservation.reservationId,
            firstName: guest.firstName.trim(),
            lastName: guest.lastName.trim(),
            gender: guest.gender,
            dateOfBirth: guest.dateOfBirth,
            phone: guest.phone.trim(),
          });
          savedGuests.push(savedGuest);
        }
      } catch (error) {
        throw new Error(
          `Reservation ${reservation.reservationId} was created, but some guest details could not be saved. ${getApiErrorMessage(error, "Please contact reception to complete guest details.")}`
        );
      }

      return { reservation, savedGuests };
    },
    onSuccess: ({ reservation,savedGuests }) => {
      const guestMessage =
        savedGuests.length > 0
          ? ` ${savedGuests.length} guest ${savedGuests.length === 1 ? "record was" : "records were"} saved.`
          : "";
    toast.success(
      `Reservation ${reservation.reservationId} created successfully. Payment has been initiated.${guestMessage}`
    );
      bookingForm.reset({ guestCount: "1", specialRequest: "", paymentMethod: "UPI" });
      setOptionalGuests([]);
      setOptionalGuestFormError(null);
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : getApiErrorMessage(error, "Failed to create reservation.");
      setOptionalGuestFormError(message);
      toast.error(message);
    },
  });

  const onSubmit = async (values: FormValues) => {
    await availabilityMutation.mutateAsync(values);
  };

  const handleBookingSubmit = (values: BookingFormValues) => {
    const guestValidationError = validateOptionalGuestEntries(optionalGuests, Number(values.guestCount));
    if (guestValidationError) {
      setOptionalGuestFormError(guestValidationError);
      toast.error(guestValidationError);
      return;
    }

    setOptionalGuestFormError(null);
    reservationMutation.mutate(values);
  };

  const handleAddGuest = () => {
    if (optionalGuests.length >= guestCountValue) {
      return;
    }
    setOptionalGuests((current) => [...current, createEmptyGuestEntry()]);
  };

  const handleRemoveGuest = (index: number) => {
    setOptionalGuests((current) => current.filter((_, currentIndex) => currentIndex !== index));
  };

  const handleGuestChange = (index: number, field: keyof OptionalGuestEntry, value: string) => {
    setOptionalGuests((current) =>
      current.map((entry, currentIndex) => (currentIndex === index ? { ...entry, [field]: value } : entry))
    );
    setOptionalGuestFormError(null);
  };

  const customerResult: AvailabilityCustomerResponseDTO | undefined = availabilityMutation.data?.search;
  const detailedResult: AvailabilityResponseDTO | undefined = availabilityMutation.data?.check;

  return (
    <div className="space-y-6">
      <PageHeader title="Search Rooms" description="Check room availability by room type and stay dates." />

      {roomTypesError ? (
        <ErrorState
          message="Unable to load room types. Please try again."
          onRetry={() => {
            void refetchRoomTypes();
          }}
        />
      ) : null}

      <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Room Type</label>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("roomTypeId")}>
            <option value="">Select room type</option>
            {roomTypes.map((roomType) => (
              <option key={roomType.roomTypeId} value={roomType.roomTypeId}>
                {roomType.name}
              </option>
            ))}
          </select>
          <FormErrorText message={errors.roomTypeId?.message} />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Check In</label>
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("checkInDate")} />
          <FormErrorText message={errors.checkInDate?.message} />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Check Out</label>
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("checkOutDate")} />
          <FormErrorText message={errors.checkOutDate?.message} />
        </div>

        <div className="flex items-end">
          <button
            type="submit"
            disabled={availabilityMutation.isPending || roomTypeLoading || roomTypesError}
            className="flex h-10 w-full items-center justify-center gap-2 rounded-md bg-slate-900 px-4 text-sm font-medium text-white disabled:opacity-60"
          >
            {availabilityMutation.isPending ? <Spinner /> : null}
            Search
          </button>
        </div>
      </form>

      {customerResult && detailedResult ? (
        <div className="grid gap-4 md:grid-cols-2">
          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Customer Availability</h3>
            <p className="mt-2 text-sm text-slate-700">{customerResult.availabilityMessage}</p>
            <dl className="mt-3 space-y-1 text-sm text-slate-600">
              <div className="flex justify-between">
                <dt>Room Type</dt>
                <dd>{customerResult.roomTypeName}</dd>
              </div>
              <div className="flex justify-between">
                <dt>Capacity</dt>
                <dd>{customerResult.capacity}</dd>
              </div>
              <div className="flex justify-between">
                <dt>Price Per Night</dt>
                <dd>{customerResult.pricePerNight}</dd>
              </div>
              <div className="flex justify-between">
                <dt>Available Rooms</dt>
                <dd>{customerResult.availableRooms}</dd>
              </div>
            </dl>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h3 className="text-base font-semibold text-slate-900">Inventory Snapshot</h3>
            <dl className="mt-3 space-y-1 text-sm text-slate-600">
              <div className="flex justify-between">
                <dt>Total Rooms</dt>
                <dd>{detailedResult.totalRooms}</dd>
              </div>
              <div className="flex justify-between">
                <dt>Booked Rooms</dt>
                <dd>{detailedResult.bookedRooms}</dd>
              </div>
              <div className="flex justify-between">
                <dt>Active Holds</dt>
                <dd>{detailedResult.activeHolds}</dd>
              </div>
              <div className="flex justify-between">
                <dt>Available Rooms</dt>
                <dd>{detailedResult.availableRooms}</dd>
              </div>
            </dl>
          </div>
        </div>
      ) : null}

      {customerResult?.available ? (
        <form
          onSubmit={bookingForm.handleSubmit(handleBookingSubmit)}
          className="grid gap-4 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4"
        >
          <h3 className="md:col-span-4 text-base font-semibold text-slate-900">Create Reservation</h3>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Guest Count</label>
            <input
              type="number"
              min={1}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              {...bookingForm.register("guestCount")}
            />
            <FormErrorText message={bookingForm.formState.errors.guestCount?.message} />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Payment Method</label>
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...bookingForm.register("paymentMethod")}>
              {paymentMethodOptions.map((method) => (
                <option key={method} value={method}>
                  {method}
                </option>
              ))}
            </select>
            <FormErrorText message={bookingForm.formState.errors.paymentMethod?.message} />
          </div>

          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-slate-700">Special Request</label>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...bookingForm.register("specialRequest")} />
            <FormErrorText message={bookingForm.formState.errors.specialRequest?.message} />
          </div>

          <OptionalGuestDetailsSection
            guestCount={guestCountValue}
            entries={optionalGuests}
            formError={optionalGuestFormError}
            onAddGuest={handleAddGuest}
            onRemoveGuest={handleRemoveGuest}
            onChangeGuest={handleGuestChange}
          />

          <div className="md:col-span-4 flex justify-end">
            <button type="submit" disabled={reservationMutation.isPending} className={primaryButtonClass}>
              {reservationMutation.isPending ? "Creating..." : "Book Room"}
            </button>
          </div>
        </form>
      ) : null}

      {reservationMutation.data ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
          Reservation {reservationMutation.data.reservation.reservationId} created successfully with status{" "}
          {reservationMutation.data.reservation.reservationStatus}. Payment has been initiated.
          {reservationMutation.data.savedGuests.length > 0
            ? ` ${reservationMutation.data.savedGuests.length} guest record(s) were saved with this booking.`
            : " You can add guest details later through reception if needed."}
        </div>
      ) : null}
    </div>
  );
}
