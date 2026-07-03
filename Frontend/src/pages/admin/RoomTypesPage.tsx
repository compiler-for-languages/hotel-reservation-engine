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
import { StatusBadge } from "@/components/common/StatusBadge";
import { RoomTypeService } from "@/services/RoomTypeService";
import type { RoomTypeResponseDTO } from "@/types/api";
import { formatCurrency } from "@/utils/format";
import { getApiErrorMessage } from "@/utils/http";

const roomTypeSchema = z.object({
  name: z.string().min(1, "Name is required."),
  description: z.string().min(1, "Description is required."),
  pricePerNight: z.string().min(1, "Price is required."),
  capacity: z.string().min(1, "Capacity is required."),
  status: z.enum(["ACTIVE", "INACTIVE"]),
});

interface FormValues {
  name: string;
  description: string;
  pricePerNight: string;
  capacity: string;
  status: "ACTIVE" | "INACTIVE";
}

export default function RoomTypesPage() {
  const queryClient = useQueryClient();
  const [editingRoomType, setEditingRoomType] = useState<RoomTypeResponseDTO | null>(null);
  const [deletingRoomTypeId, setDeletingRoomTypeId] = useState<number | null>(null);
  const [lookupRoomTypeId, setLookupRoomTypeId] = useState("");
  const [lookupRoomTypeName, setLookupRoomTypeName] = useState("");

  const createForm = useForm<FormValues>({
    resolver: zodResolver(roomTypeSchema),
    defaultValues: {
      name: "",
      description: "",
      pricePerNight: "0",
      capacity: "1",
      status: "ACTIVE",
    },
  });

  const editForm = useForm<FormValues>({
    resolver: zodResolver(roomTypeSchema),
    defaultValues: {
      name: "",
      description: "",
      pricePerNight: "0",
      capacity: "1",
      status: "ACTIVE",
    },
  });

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["roomTypes"],
    queryFn: RoomTypeService.getAllRoomTypes,
  });

  const createMutation = useMutation({
    mutationFn: (payload: FormValues) =>
      RoomTypeService.createRoomType({
        name: payload.name,
        description: payload.description,
        pricePerNight: Number(payload.pricePerNight),
        capacity: Number(payload.capacity),
        status: payload.status,
      }),
    onSuccess: () => {
      toast.success("Room type created successfully.");
      createForm.reset({ name: "", description: "", pricePerNight: "0", capacity: "1", status: "ACTIVE" });
      void queryClient.invalidateQueries({ queryKey: ["roomTypes"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to create room type."));
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload: FormValues) => {
      if (!editingRoomType) {
        throw new Error("No room type selected.");
      }
      return RoomTypeService.updateRoomType(editingRoomType.roomTypeId, {
        name: payload.name,
        description: payload.description,
        pricePerNight: Number(payload.pricePerNight),
        capacity: Number(payload.capacity),
        status: payload.status,
      });
    },
    onSuccess: () => {
      toast.success("Room type updated successfully.");
      setEditingRoomType(null);
      void queryClient.invalidateQueries({ queryKey: ["roomTypes"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update room type."));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (roomTypeId: number) => RoomTypeService.deleteRoomType(roomTypeId),
    onSuccess: () => {
      toast.success("Room type deleted successfully.");
      void queryClient.invalidateQueries({ queryKey: ["roomTypes"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to delete room type."));
    },
  });

  const openEditModal = (roomType: RoomTypeResponseDTO) => {
    setEditingRoomType(roomType);
    editForm.reset({
      name: roomType.name,
      description: roomType.description,
      pricePerNight: String(roomType.pricePerNight),
      capacity: String(roomType.capacity),
      status: roomType.status,
    });
  };

  const getByIdMutation = useMutation({
    mutationFn: (roomTypeId: number) => RoomTypeService.getRoomTypeById(roomTypeId),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to find room type by ID.")),
  });

  const getByNameMutation = useMutation({
    mutationFn: (name: string) => RoomTypeService.getRoomTypeByName(name),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to find room type by name.")),
  });

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-semibold text-slate-900">Room Types</h2>

      <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-2">
        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900">Get Room Type By ID</h3>
          <div className="flex gap-2">
            <input value={lookupRoomTypeId} onChange={(event) => setLookupRoomTypeId(event.target.value)} type="number" placeholder="Room type ID" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            <button
              type="button"
              onClick={() => {
                if (!lookupRoomTypeId) {
                  toast.error("Room type ID is required.");
                  return;
                }
                getByIdMutation.mutate(Number(lookupRoomTypeId));
              }}
              className="rounded-md bg-slate-900 px-3 py-2 text-sm text-white"
            >
              Find
            </button>
          </div>
          {getByIdMutation.data ? <p className="text-sm text-slate-600">{getByIdMutation.data.name} - {formatCurrency(getByIdMutation.data.pricePerNight)}</p> : null}
        </div>

        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900">Get Room Type By Name</h3>
          <div className="flex gap-2">
            <input value={lookupRoomTypeName} onChange={(event) => setLookupRoomTypeName(event.target.value)} placeholder="Room type name" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            <button
              type="button"
              onClick={() => {
                if (!lookupRoomTypeName.trim()) {
                  toast.error("Room type name is required.");
                  return;
                }
                getByNameMutation.mutate(lookupRoomTypeName.trim());
              }}
              className="rounded-md bg-slate-900 px-3 py-2 text-sm text-white"
            >
              Find
            </button>
          </div>
          {getByNameMutation.data ? <p className="text-sm text-slate-600">ID {getByNameMutation.data.roomTypeId} - {getByNameMutation.data.status}</p> : null}
        </div>
      </div>

      <form onSubmit={createForm.handleSubmit((values) => createMutation.mutate(values))} className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-5">
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...createForm.register("name")} />
          <FormErrorText message={createForm.formState.errors.name?.message} />
        </div>
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Description" {...createForm.register("description")} />
          <FormErrorText message={createForm.formState.errors.description?.message} />
        </div>
        <div>
          <input type="number" step="0.01" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Price per night" {...createForm.register("pricePerNight")} />
          <FormErrorText message={createForm.formState.errors.pricePerNight?.message} />
        </div>
        <div>
          <input type="number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Capacity" {...createForm.register("capacity")} />
          <FormErrorText message={createForm.formState.errors.capacity?.message} />
        </div>
        <div>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...createForm.register("status")}>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
          <FormErrorText message={createForm.formState.errors.status?.message} />
        </div>
        <div className="md:col-span-5 flex items-center justify-between text-xs text-rose-600">
          <span />
          <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={createMutation.isPending}>
            {createMutation.isPending ? "Creating..." : "Create Room Type"}
          </button>
        </div>
      </form>
      <DataTable
        title="Room Type Catalog"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load room types." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No room types configured yet."
        rowKey={(row) => row.roomTypeId}
        searchPlaceholder="Search by name or description"
        searchTextExtractor={(row) => `${row.name} ${row.description} ${row.status}`}
        columns={[
          { key: "id", header: "ID", render: (row) => row.roomTypeId, sortValue: (row) => row.roomTypeId },
          { key: "name", header: "Name", render: (row) => row.name, sortValue: (row) => row.name },
          { key: "description", header: "Description", render: (row) => row.description, sortValue: (row) => row.description },
          { key: "price", header: "Price/Night", render: (row) => formatCurrency(row.pricePerNight), sortValue: (row) => row.pricePerNight },
          { key: "capacity", header: "Capacity", render: (row) => row.capacity, sortValue: (row) => row.capacity },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.status} />, sortValue: (row) => row.status },
          {
            key: "actions",
            header: "Actions",
            render: (row) => (
              <div className="flex gap-2">
                <button type="button" className="rounded border border-slate-300 px-2 py-1 text-xs" onClick={() => openEditModal(row)}>
                  Edit
                </button>
                <button
                  type="button"
                  className="rounded border border-rose-200 px-2 py-1 text-xs text-rose-700"
                  onClick={() => setDeletingRoomTypeId(row.roomTypeId)}
                >
                  Delete
                </button>
              </div>
            ),
          },
        ]}
      />

      <Modal open={Boolean(editingRoomType)} title="Edit Room Type" onClose={() => setEditingRoomType(null)}>
        <form className="grid gap-3 md:grid-cols-2" onSubmit={editForm.handleSubmit((values) => updateMutation.mutate(values))}>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...editForm.register("name")} />
            <FormErrorText message={editForm.formState.errors.name?.message} />
          </div>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Description" {...editForm.register("description")} />
            <FormErrorText message={editForm.formState.errors.description?.message} />
          </div>
          <div>
            <input type="number" step="0.01" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Price per night" {...editForm.register("pricePerNight")} />
            <FormErrorText message={editForm.formState.errors.pricePerNight?.message} />
          </div>
          <div>
            <input type="number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Capacity" {...editForm.register("capacity")} />
            <FormErrorText message={editForm.formState.errors.capacity?.message} />
          </div>
          <div className="md:col-span-2">
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...editForm.register("status")}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
            <FormErrorText message={editForm.formState.errors.status?.message} />
          </div>
          <div className="md:col-span-2 flex justify-end">
            <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? "Updating..." : "Save Changes"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deletingRoomTypeId)}
        title="Delete Room Type"
        description="Deleting this room type may impact existing room mappings. Continue only if this room type is no longer needed."
        confirmLabel="Delete"
        isConfirming={deleteMutation.isPending}
        onCancel={() => setDeletingRoomTypeId(null)}
        onConfirm={() => {
          if (deletingRoomTypeId) {
            deleteMutation.mutate(deletingRoomTypeId, {
              onSettled: () => setDeletingRoomTypeId(null),
            });
          }
        }}
      />
    </div>
  );
}