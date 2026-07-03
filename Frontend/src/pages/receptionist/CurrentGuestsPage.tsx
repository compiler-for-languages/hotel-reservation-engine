import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { z } from "zod";
import { DataTable } from "@/components/common/DataTable";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { FormErrorText } from "@/components/common/FormErrorText";
import { Modal } from "@/components/common/Modal";
import { PageHeader } from "@/components/common/PageHeader";
import { GuestService } from "@/services/GuestService";
import { ReceptionService } from "@/services/ReceptionService";
import type { Gender, GuestResponseDTO } from "@/types/api";
import { formatDate, formatDateTime } from "@/utils/format";
import { getApiErrorMessage } from "@/utils/http";
import { primaryButtonClass, secondaryButtonClass, tableActionButtonClass, tableDangerButtonClass } from "@/utils/ui";

const searchSchema = z.object({
  reservationId: z.string().min(1, "Reservation ID is required."),
});

const guestSchema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  phone: z.string().min(7, "Phone is required."),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  dateOfBirth: z.string().min(1, "Date of birth is required."),
});

interface SearchFormValues {
  reservationId: string;
}
type GuestFormValues = z.infer<typeof guestSchema>;

const genderOptions: Gender[] = ["MALE", "FEMALE", "OTHER"];

export default function CurrentGuestsPage() {
  const queryClient = useQueryClient();
  const [editingGuest, setEditingGuest] = useState<GuestResponseDTO | null>(null);
  const [deletingGuestId, setDeletingGuestId] = useState<number | null>(null);
  const [lookupGuestId, setLookupGuestId] = useState("");

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["currentGuests"],
    queryFn: ReceptionService.getCurrentGuests,
  });

  const guestSearchForm = useForm<SearchFormValues>({
    resolver: zodResolver(searchSchema),
    defaultValues: { reservationId: "" },
  });

  const guestCreateForm = useForm<GuestFormValues>({
    resolver: zodResolver(guestSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      phone: "",
      gender: "MALE",
      dateOfBirth: "",
    },
  });

  const guestEditForm = useForm<GuestFormValues>({
    resolver: zodResolver(guestSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      phone: "",
      gender: "MALE",
      dateOfBirth: "",
    },
  });

  const reservationId = Number(guestSearchForm.watch("reservationId"));

  const {
    data: reservationGuests = [],
    isFetching: isGuestListLoading,
    error: reservationGuestsError,
    refetch: refetchReservationGuests,
  } = useQuery({
    queryKey: ["guestsByReservation", reservationId],
    queryFn: () => GuestService.getGuestsByReservation(reservationId),
    enabled: reservationId > 0,
  });

  const {
    data: allGuests = [],
    isLoading: allGuestsLoading,
    error: allGuestsError,
    refetch: refetchAllGuests,
  } = useQuery({
    queryKey: ["allGuests"],
    queryFn: GuestService.getAllGuests,
  });

  const createGuestMutation = useMutation({
    mutationFn: (values: GuestFormValues) =>
      GuestService.createGuest({
        reservationId,
        firstName: values.firstName,
        lastName: values.lastName,
        phone: values.phone,
        gender: values.gender,
        dateOfBirth: values.dateOfBirth,
      }),
    onSuccess: () => {
      toast.success("Guest added successfully.");
      guestCreateForm.reset({ firstName: "", lastName: "", phone: "", gender: "MALE", dateOfBirth: "" });
      void queryClient.invalidateQueries({ queryKey: ["guestsByReservation", reservationId] });
      void queryClient.invalidateQueries({ queryKey: ["currentGuests"] });
      void queryClient.invalidateQueries({ queryKey: ["allGuests"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Failed to add guest.")),
  });

  const deleteGuestMutation = useMutation({
    mutationFn: (guestId: number) => GuestService.deleteGuest(guestId),
    onSuccess: () => {
      toast.success("Guest deleted successfully.");
      void queryClient.invalidateQueries({ queryKey: ["guestsByReservation", reservationId] });
      void queryClient.invalidateQueries({ queryKey: ["currentGuests"] });
      void queryClient.invalidateQueries({ queryKey: ["allGuests"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Failed to delete guest.")),
  });

  const updateGuestMutation = useMutation({
    mutationFn: (values: GuestFormValues) => {
      if (!editingGuest) {
        throw new Error("No guest selected.");
      }
      return GuestService.updateGuest(editingGuest.guestId, values);
    },
    onSuccess: () => {
      toast.success("Guest updated successfully.");
      setEditingGuest(null);
      void queryClient.invalidateQueries({ queryKey: ["guestsByReservation", reservationId] });
      void queryClient.invalidateQueries({ queryKey: ["currentGuests"] });
      void queryClient.invalidateQueries({ queryKey: ["allGuests"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Failed to update guest.")),
  });

  const openEditModal = (guest: GuestResponseDTO) => {
    setEditingGuest(guest);
    guestEditForm.reset({
      firstName: guest.firstName,
      lastName: guest.lastName,
      phone: guest.phone,
      gender: guest.gender,
      dateOfBirth: guest.dateOfBirth,
    });
  };

  const getGuestByIdMutation = useMutation({
    mutationFn: (guestId: number) => GuestService.getGuestById(guestId),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fetch guest by ID.")),
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Current Guests"
        description="Manage in-house guests and add or update guest details by reservation."
      />

      <DataTable
        title="In-House Guests"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load current guests." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="There are no checked-in guests right now."
        rowKey={(row) => row.reservationId}
        searchPlaceholder="Search by customer, room, room type"
        searchTextExtractor={(row) => `${row.primaryCustomerName} ${row.roomNumber} ${row.roomType}`}
        columns={[
          { key: "reservationId", header: "Reservation ID", render: (row) => row.reservationId, sortValue: (row) => row.reservationId },
          {
            key: "customer",
            header: "Primary Customer",
            render: (row) => row.primaryCustomerName,
            sortValue: (row) => row.primaryCustomerName,
          },
          { key: "roomNumber", header: "Room", render: (row) => row.roomNumber, sortValue: (row) => row.roomNumber },
          { key: "roomType", header: "Room Type", render: (row) => row.roomType, sortValue: (row) => row.roomType },
          { key: "checkInDate", header: "Check In", render: (row) => formatDate(row.checkInDate), sortValue: (row) => row.checkInDate },
          { key: "checkOutDate", header: "Check Out", render: (row) => formatDate(row.checkOutDate), sortValue: (row) => row.checkOutDate },
          {
            key: "actualCheckIn",
            header: "Actual Check In",
            render: (row) => (row.actualCheckIn ? formatDateTime(row.actualCheckIn) : "-"),
            sortValue: (row) => row.actualCheckIn || "",
          },
          {
            key: "guestCount",
            header: "Guests",
            render: (row) => `${row.guests.length} (${row.guests.map((item) => `${item.firstName} ${item.lastName}`).join(", ")})`,
            sortValue: (row) => row.guests.length,
          },
        ]}
      />

      <div className="grid gap-4 lg:grid-cols-2">
        <form
          onSubmit={guestSearchForm.handleSubmit(() => undefined)}
          className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
        >
          <h3 className="text-base font-semibold text-slate-900">Guest List By Reservation</h3>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Reservation ID</label>
            <input
              type="number"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              {...guestSearchForm.register("reservationId")}
            />
            <FormErrorText message={guestSearchForm.formState.errors.reservationId?.message} />
          </div>

          {isGuestListLoading ? <p className="text-sm text-slate-500">Loading guests...</p> : null}
          {reservationGuestsError ? (
            <button
              type="button"
              className="rounded border border-slate-300 px-3 py-1 text-xs"
              onClick={() => {
                void refetchReservationGuests();
              }}
            >
              Retry Guest Fetch
            </button>
          ) : null}

          <div className="space-y-2">
            {reservationGuests.map((guest) => (
              <div key={guest.guestId} className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2 text-sm">
                <div>
                  <p className="font-medium text-slate-900">
                    {guest.firstName} {guest.lastName}
                  </p>
                  <p className="text-xs text-slate-500">
                    {guest.gender} | {guest.phone} | {guest.dateOfBirth}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button type="button" onClick={() => openEditModal(guest)} className="rounded border border-slate-300 px-2 py-1 text-xs">
                    Edit
                  </button>
                  <button
                    type="button"
                    onClick={() => setDeletingGuestId(guest.guestId)}
                    className="rounded border border-rose-200 px-2 py-1 text-xs text-rose-700"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {reservationId > 0 && reservationGuests.length === 0 && !isGuestListLoading ? (
              <p className="text-sm text-slate-500">No guests found for this reservation.</p>
            ) : null}
          </div>
        </form>

        <form
          onSubmit={guestCreateForm.handleSubmit((values) => createGuestMutation.mutate(values))}
          className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
        >
          <h3 className="text-base font-semibold text-slate-900">Add Guest</h3>
          <p className="text-xs text-slate-500">Creates guest for the selected reservation ID.</p>

          <div className="grid gap-3 md:grid-cols-2">
            <div>
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="First Name" {...guestCreateForm.register("firstName")} />
              <FormErrorText message={guestCreateForm.formState.errors.firstName?.message} />
            </div>
            <div>
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Last Name" {...guestCreateForm.register("lastName")} />
              <FormErrorText message={guestCreateForm.formState.errors.lastName?.message} />
            </div>
            <div>
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" {...guestCreateForm.register("phone")} />
              <FormErrorText message={guestCreateForm.formState.errors.phone?.message} />
            </div>
            <div>
              <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...guestCreateForm.register("gender")}>
                {genderOptions.map((gender) => (
                  <option key={gender} value={gender}>
                    {gender}
                  </option>
                ))}
              </select>
              <FormErrorText message={guestCreateForm.formState.errors.gender?.message} />
            </div>
            <div className="md:col-span-2">
              <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...guestCreateForm.register("dateOfBirth")} />
              <FormErrorText message={guestCreateForm.formState.errors.dateOfBirth?.message} />
            </div>
          </div>

          <button
            type="submit"
            disabled={createGuestMutation.isPending || reservationId <= 0}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {createGuestMutation.isPending ? "Adding..." : "Add Guest"}
          </button>
        </form>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h3 className="text-base font-semibold text-slate-900">Get Guest By ID</h3>
          <div className="flex gap-2">
            <input value={lookupGuestId} onChange={(event) => setLookupGuestId(event.target.value)} type="number" placeholder="Guest ID" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            <button
              type="button"
              onClick={() => {
                if (!lookupGuestId) {
                  toast.error("Guest ID is required.");
                  return;
                }
                getGuestByIdMutation.mutate(Number(lookupGuestId));
              }}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
            >
              Find
            </button>
          </div>

          {getGuestByIdMutation.data ? (
            <div className="rounded border border-slate-200 p-3 text-sm text-slate-700">
              <p>ID: {getGuestByIdMutation.data.guestId}</p>
              <p>Name: {getGuestByIdMutation.data.firstName} {getGuestByIdMutation.data.lastName}</p>
              <p>Reservation: {getGuestByIdMutation.data.reservationId}</p>
              <p>Phone: {getGuestByIdMutation.data.phone}</p>
            </div>
          ) : null}
        </div>

        <DataTable
          title="All Guests"
          data={allGuests}
          isLoading={allGuestsLoading}
          errorMessage={allGuestsError ? "Unable to load guest list." : null}
          onRetry={() => {
            void refetchAllGuests();
          }}
          emptyMessage="No guests found in system."
          rowKey={(row) => `all-${row.guestId}`}
          searchTextExtractor={(row) => `${row.guestId} ${row.firstName} ${row.lastName} ${row.phone}`}
          columns={[
            { key: "id", header: "Guest ID", render: (row) => row.guestId },
            { key: "name", header: "Name", render: (row) => `${row.firstName} ${row.lastName}` },
            { key: "reservation", header: "Reservation", render: (row) => row.reservationId },
            { key: "phone", header: "Phone", render: (row) => row.phone },
          ]}
        />
      </div>

      <Modal open={Boolean(editingGuest)} title="Edit Guest" onClose={() => setEditingGuest(null)}>
        <form className="grid gap-3 md:grid-cols-2" onSubmit={guestEditForm.handleSubmit((values) => updateGuestMutation.mutate(values))}>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="First Name" {...guestEditForm.register("firstName")} />
            <FormErrorText message={guestEditForm.formState.errors.firstName?.message} />
          </div>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Last Name" {...guestEditForm.register("lastName")} />
            <FormErrorText message={guestEditForm.formState.errors.lastName?.message} />
          </div>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" {...guestEditForm.register("phone")} />
            <FormErrorText message={guestEditForm.formState.errors.phone?.message} />
          </div>
          <div>
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...guestEditForm.register("gender")}>
              {genderOptions.map((gender) => (
                <option key={gender} value={gender}>
                  {gender}
                </option>
              ))}
            </select>
            <FormErrorText message={guestEditForm.formState.errors.gender?.message} />
          </div>
          <div className="md:col-span-2">
            <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...guestEditForm.register("dateOfBirth")} />
            <FormErrorText message={guestEditForm.formState.errors.dateOfBirth?.message} />
          </div>
          <div className="md:col-span-2 flex justify-end">
            <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={updateGuestMutation.isPending}>
              {updateGuestMutation.isPending ? "Updating..." : "Save Changes"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deletingGuestId)}
        title="Delete Guest"
        description="This will permanently delete guest information from the reservation."
        confirmLabel="Delete"
        isConfirming={deleteGuestMutation.isPending}
        onCancel={() => setDeletingGuestId(null)}
        onConfirm={() => {
          if (deletingGuestId) {
            deleteGuestMutation.mutate(deletingGuestId, {
              onSettled: () => setDeletingGuestId(null),
            });
          }
        }}
      />
    </div>
  );
}