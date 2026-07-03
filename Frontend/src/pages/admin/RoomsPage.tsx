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
import { RoomService } from "@/services/RoomService";
import { RoomTypeService } from "@/services/RoomTypeService";
import type { RoomResponseDTO, RoomStatus } from "@/types/api";
import { getApiErrorMessage } from "@/utils/http";

const roomCreateSchema = z.object({
  roomNumber: z.string().min(1, "Room number is required."),
  roomTypeId: z.string().min(1, "Room type is required."),
  floorNumber: z.string().min(1, "Floor number is required."),
});

const roomEditSchema = z.object({
  roomNumber: z.string().min(1, "Room number is required."),
  roomStatus: z.enum(["AVAILABLE", "OCCUPIED", "MAINTENANCE", "OUT_OF_SERVICE"]),
});

interface FormValues {
  roomNumber: string;
  roomTypeId: string;
  floorNumber: string;
}

interface EditFormValues {
  roomNumber: string;
  roomStatus: RoomStatus;
}

export default function RoomsPage() {
  const queryClient = useQueryClient();
  const [editingRoom, setEditingRoom] = useState<RoomResponseDTO | null>(null);
  const [deletingRoomId, setDeletingRoomId] = useState<number | null>(null);
  const [lookupRoomId, setLookupRoomId] = useState("");
  const [lookupRoomNumber, setLookupRoomNumber] = useState("");
  const [filterRoomTypeId, setFilterRoomTypeId] = useState("");
  const [filterRoomStatus, setFilterRoomStatus] = useState<RoomStatus>("AVAILABLE");

  const createForm = useForm<FormValues>({
    resolver: zodResolver(roomCreateSchema),
    defaultValues: {
      roomNumber: "",
      roomTypeId: "",
      floorNumber: "1",
    },
  });

  const editForm = useForm<EditFormValues>({
    resolver: zodResolver(roomEditSchema),
    defaultValues: {
      roomNumber: "",
      roomStatus: "AVAILABLE",
    },
  });

  const {
    data: rooms = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["rooms"],
    queryFn: RoomService.getAllRooms,
  });

  const { data: roomTypes = [] } = useQuery({
    queryKey: ["roomTypes"],
    queryFn: RoomTypeService.getAllRoomTypes,
  });

  const createMutation = useMutation({
    mutationFn: (payload: FormValues) =>
      RoomService.createRoom({
        roomNumber: payload.roomNumber,
        roomTypeId: Number(payload.roomTypeId),
        floorNumber: Number(payload.floorNumber),
        roomStatus: "AVAILABLE",
      }),
    onSuccess: () => {
      toast.success("Room created successfully.");
      createForm.reset({ roomNumber: "", roomTypeId: "", floorNumber: "1" });
      void queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to create room."));
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload: EditFormValues) => {
      if (!editingRoom) {
        throw new Error("No room selected.");
      }
      return RoomService.updateRoom(editingRoom.roomId, payload);
    },
    onSuccess: () => {
      toast.success("Room updated successfully.");
      setEditingRoom(null);
      void queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update room."));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (roomId: number) => RoomService.deleteRoom(roomId),
    onSuccess: () => {
      toast.success("Room deleted successfully.");
      void queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to delete room."));
    },
  });

  const openEditModal = (room: RoomResponseDTO) => {
    setEditingRoom(room);
    editForm.reset({
      roomNumber: room.roomNumber,
      roomStatus: room.roomStatus,
    });
  };

  const getByIdMutation = useMutation({
    mutationFn: (roomId: number) => RoomService.getRoomById(roomId),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to find room by ID.")),
  });

  const getByNumberMutation = useMutation({
    mutationFn: (roomNumber: string) => RoomService.getRoomByRoomNumber(roomNumber),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to find room by number.")),
  });

  const byRoomTypeMutation = useMutation({
    mutationFn: (roomTypeId: number) => RoomService.getRoomsByRoomType(roomTypeId),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to fetch rooms by room type.")),
  });

  const filterMutation = useMutation({
    mutationFn: (payload: { roomTypeId: number; roomStatus: RoomStatus }) => RoomService.getRoomsByRoomTypeAndStatus(payload.roomTypeId, payload.roomStatus),
    onError: (error) => toast.error(getApiErrorMessage(error, "Unable to filter rooms.")),
  });

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-semibold text-slate-900">Rooms</h2>

      <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-2">
        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900">Get Room By ID</h3>
          <div className="flex gap-2">
            <input value={lookupRoomId} onChange={(event) => setLookupRoomId(event.target.value)} type="number" placeholder="Room ID" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            <button
              type="button"
              onClick={() => {
                if (!lookupRoomId) {
                  toast.error("Room ID is required.");
                  return;
                }
                getByIdMutation.mutate(Number(lookupRoomId));
              }}
              className="rounded-md bg-slate-900 px-3 py-2 text-sm text-white"
            >
              Find
            </button>
          </div>
          {getByIdMutation.data ? <p className="text-sm text-slate-600">Room {getByIdMutation.data.roomNumber} - {getByIdMutation.data.roomStatus}</p> : null}
        </div>

        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900">Get Room By Number</h3>
          <div className="flex gap-2">
            <input value={lookupRoomNumber} onChange={(event) => setLookupRoomNumber(event.target.value)} placeholder="Room number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            <button
              type="button"
              onClick={() => {
                if (!lookupRoomNumber.trim()) {
                  toast.error("Room number is required.");
                  return;
                }
                getByNumberMutation.mutate(lookupRoomNumber.trim());
              }}
              className="rounded-md bg-slate-900 px-3 py-2 text-sm text-white"
            >
              Find
            </button>
          </div>
          {getByNumberMutation.data ? <p className="text-sm text-slate-600">ID {getByNumberMutation.data.roomId} - Floor {getByNumberMutation.data.floorNumber}</p> : null}
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4">
        <select className="rounded-md border border-slate-300 px-3 py-2 text-sm" value={filterRoomTypeId} onChange={(event) => setFilterRoomTypeId(event.target.value)}>
          <option value="">Select room type</option>
          {roomTypes.map((roomType) => (
            <option key={roomType.roomTypeId} value={roomType.roomTypeId}>
              {roomType.name}
            </option>
          ))}
        </select>
        <button
          type="button"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          onClick={() => {
            if (!filterRoomTypeId) {
              toast.error("Room type is required.");
              return;
            }
            byRoomTypeMutation.mutate(Number(filterRoomTypeId));
          }}
        >
          Get By Room Type
        </button>
        <select className="rounded-md border border-slate-300 px-3 py-2 text-sm" value={filterRoomStatus} onChange={(event) => setFilterRoomStatus(event.target.value as RoomStatus)}>
          <option value="AVAILABLE">AVAILABLE</option>
          <option value="OCCUPIED">OCCUPIED</option>
          <option value="MAINTENANCE">MAINTENANCE</option>
          <option value="OUT_OF_SERVICE">OUT_OF_SERVICE</option>
        </select>
        <button
          type="button"
          className="rounded-md bg-slate-900 px-3 py-2 text-sm text-white"
          onClick={() => {
            if (!filterRoomTypeId) {
              toast.error("Room type is required.");
              return;
            }
            filterMutation.mutate({ roomTypeId: Number(filterRoomTypeId), roomStatus: filterRoomStatus });
          }}
        >
          Filter Rooms
        </button>
      </div>

      {byRoomTypeMutation.data ? (
        <DataTable
          title="Rooms By Room Type"
          data={byRoomTypeMutation.data}
          rowKey={(row) => `byType-${row.roomId}`}
          searchTextExtractor={(row) => `${row.roomNumber} ${row.roomStatus}`}
          emptyMessage="No rooms found for selected room type."
          columns={[
            { key: "id", header: "ID", render: (row) => row.roomId },
            { key: "number", header: "Room", render: (row) => row.roomNumber },
            { key: "status", header: "Status", render: (row) => <StatusBadge value={row.roomStatus} /> },
          ]}
        />
      ) : null}

      {filterMutation.data ? (
        <DataTable
          title="Filtered Rooms"
          data={filterMutation.data}
          rowKey={(row) => `filtered-${row.roomId}`}
          searchTextExtractor={(row) => `${row.roomNumber} ${row.roomStatus}`}
          emptyMessage="No rooms match selected filter."
          columns={[
            { key: "id", header: "ID", render: (row) => row.roomId },
            { key: "number", header: "Room", render: (row) => row.roomNumber },
            { key: "status", header: "Status", render: (row) => <StatusBadge value={row.roomStatus} /> },
          ]}
        />
      ) : null}

      <form onSubmit={createForm.handleSubmit((values) => createMutation.mutate(values))} className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-4">
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Room Number" {...createForm.register("roomNumber")} />
          <FormErrorText message={createForm.formState.errors.roomNumber?.message} />
        </div>
        <div>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...createForm.register("roomTypeId")}>
            <option value="">Select room type</option>
            {roomTypes.map((roomType) => (
              <option key={roomType.roomTypeId} value={roomType.roomTypeId}>
                {roomType.name}
              </option>
            ))}
          </select>
          <FormErrorText message={createForm.formState.errors.roomTypeId?.message} />
        </div>
        <div>
          <input type="number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Floor" {...createForm.register("floorNumber")} />
          <FormErrorText message={createForm.formState.errors.floorNumber?.message} />
        </div>
        <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={createMutation.isPending}>
          {createMutation.isPending ? "Creating..." : "Create Room"}
        </button>
      </form>
      <DataTable
        title="Room Inventory"
        data={rooms}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load rooms." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No rooms found in inventory."
        rowKey={(row) => row.roomId}
        searchPlaceholder="Search by room number or room type"
        searchTextExtractor={(row) => `${row.roomNumber} ${row.roomTypeName} ${row.roomStatus}`}
        columns={[
          { key: "id", header: "ID", render: (row) => row.roomId, sortValue: (row) => row.roomId },
          { key: "number", header: "Room Number", render: (row) => row.roomNumber, sortValue: (row) => row.roomNumber },
          { key: "roomType", header: "Room Type", render: (row) => row.roomTypeName, sortValue: (row) => row.roomTypeName },
          { key: "floor", header: "Floor", render: (row) => row.floorNumber, sortValue: (row) => row.floorNumber },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.roomStatus} />, sortValue: (row) => row.roomStatus },
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
                  onClick={() => setDeletingRoomId(row.roomId)}
                >
                  Delete
                </button>
              </div>
            ),
          },
        ]}
      />

      <Modal open={Boolean(editingRoom)} title="Edit Room" onClose={() => setEditingRoom(null)}>
        <form className="grid gap-3" onSubmit={editForm.handleSubmit((values) => updateMutation.mutate(values))}>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Room Number" {...editForm.register("roomNumber")} />
            <FormErrorText message={editForm.formState.errors.roomNumber?.message} />
          </div>
          <div>
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...editForm.register("roomStatus")}>
              <option value="AVAILABLE">AVAILABLE</option>
              <option value="OCCUPIED">OCCUPIED</option>
              <option value="MAINTENANCE">MAINTENANCE</option>
              <option value="OUT_OF_SERVICE">OUT_OF_SERVICE</option>
            </select>
            <FormErrorText message={editForm.formState.errors.roomStatus?.message} />
          </div>
          <div className="flex justify-end">
            <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? "Updating..." : "Save Changes"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deletingRoomId)}
        title="Delete Room"
        description="This will remove the selected room from inventory. Ensure it is not active in operations before deleting."
        confirmLabel="Delete"
        isConfirming={deleteMutation.isPending}
        onCancel={() => setDeletingRoomId(null)}
        onConfirm={() => {
          if (deletingRoomId) {
            deleteMutation.mutate(deletingRoomId, {
              onSettled: () => setDeletingRoomId(null),
            });
          }
        }}
      />
    </div>
  );
}