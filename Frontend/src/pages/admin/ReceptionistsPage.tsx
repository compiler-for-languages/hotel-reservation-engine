import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { z } from "zod";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { DataTable } from "@/components/common/DataTable";
import { FormErrorText } from "@/components/common/FormErrorText";
import { Modal } from "@/components/common/Modal";
import { StatusBadge } from "@/components/common/StatusBadge";
import { UserService } from "@/services/UserService";
import type { UserResponseDTO } from "@/types/api";
import { getApiErrorMessage } from "@/utils/http";

const createSchema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  email: z.string().email("Enter a valid email."),
  phone: z.string().min(7, "Phone is required."),
  password: z.string().min(6, "Password must be at least 6 characters."),
});

const patchSchema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  phone: z.string().min(7, "Phone is required."),
  accountStatus: z.enum(["ACTIVE", "INACTIVE"]),
});

interface CreateFormValues {
  firstName: string;
  lastName: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  email: string;
  phone: string;
  password: string;
}

type EditFormValues = z.infer<typeof patchSchema>;

export default function ReceptionistsPage() {
  const queryClient = useQueryClient();
  const [editingReceptionist, setEditingReceptionist] = useState<UserResponseDTO | null>(null);
  const [deletingReceptionistId, setDeletingReceptionistId] = useState<number | null>(null);

  const createForm = useForm<CreateFormValues>({
    resolver: zodResolver(createSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      gender: "MALE",
      email: "",
      phone: "",
      password: "",
    },
  });

  const editForm = useForm<EditFormValues>({
    resolver: zodResolver(patchSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      phone: "",
      accountStatus: "ACTIVE",
    },
  });

  const {
    data = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["adminReceptionists"],
    queryFn: UserService.getAllReceptionists,
  });

  const createMutation = useMutation({
    mutationFn: (payload: CreateFormValues) =>
      UserService.createReceptionist({
        ...payload,
        role: "RECEPTIONIST",
        accountStatus: "ACTIVE",
      }),
    onSuccess: () => {
      toast.success("Receptionist created successfully.");
      createForm.reset();
      void queryClient.invalidateQueries({ queryKey: ["adminReceptionists"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to create receptionist."));
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload: EditFormValues) => {
      if (!editingReceptionist) {
        throw new Error("No receptionist selected.");
      }
      return UserService.updateUser(editingReceptionist.userId, payload);
    },
    onSuccess: () => {
      toast.success("Receptionist updated successfully.");
      setEditingReceptionist(null);
      void queryClient.invalidateQueries({ queryKey: ["adminReceptionists"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update receptionist."));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (userId: number) => UserService.deleteUser(userId),
    onSuccess: () => {
      toast.success("Receptionist deleted successfully.");
      void queryClient.invalidateQueries({ queryKey: ["adminReceptionists"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to delete receptionist."));
    },
  });

  const openEditModal = (receptionist: UserResponseDTO) => {
    setEditingReceptionist(receptionist);
    editForm.reset({
      firstName: receptionist.firstName,
      lastName: receptionist.lastName,
      phone: receptionist.phone,
      accountStatus: receptionist.accountStatus,
    });
  };

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-semibold text-slate-900">Receptionists</h2>

      <form onSubmit={createForm.handleSubmit((values) => createMutation.mutate(values))} className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm md:grid-cols-3">
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="First Name" {...createForm.register("firstName")} />
          <FormErrorText message={createForm.formState.errors.firstName?.message} />
        </div>
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Last Name" {...createForm.register("lastName")} />
          <FormErrorText message={createForm.formState.errors.lastName?.message} />
        </div>
        <div>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...createForm.register("gender")}>
            <option value="MALE">MALE</option>
            <option value="FEMALE">FEMALE</option>
            <option value="OTHER">OTHER</option>
          </select>
          <FormErrorText message={createForm.formState.errors.gender?.message} />
        </div>
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Email" {...createForm.register("email")} />
          <FormErrorText message={createForm.formState.errors.email?.message} />
        </div>
        <div>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" {...createForm.register("phone")} />
          <FormErrorText message={createForm.formState.errors.phone?.message} />
        </div>
        <div>
          <input type="password" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Password" {...createForm.register("password")} />
          <FormErrorText message={createForm.formState.errors.password?.message} />
        </div>
        <div className="md:col-span-3">
          <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={createMutation.isPending}>
            {createMutation.isPending ? "Creating..." : "Create Receptionist"}
          </button>
        </div>
      </form>

      <DataTable
        title="Receptionist List"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load receptionists." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No receptionists available yet."
        rowKey={(row) => row.userId}
        searchPlaceholder="Search by name, email, phone"
        searchTextExtractor={(row) => `${row.firstName} ${row.lastName} ${row.email} ${row.phone}`}
        columns={[
          { key: "id", header: "ID", render: (row) => row.userId, sortValue: (row) => row.userId },
          { key: "name", header: "Name", render: (row) => `${row.firstName} ${row.lastName}`, sortValue: (row) => `${row.firstName} ${row.lastName}` },
          { key: "email", header: "Email", render: (row) => row.email, sortValue: (row) => row.email },
          { key: "phone", header: "Phone", render: (row) => row.phone, sortValue: (row) => row.phone },
          { key: "role", header: "Role", render: (row) => row.role, sortValue: (row) => row.role },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.accountStatus} />, sortValue: (row) => row.accountStatus },
          {
            key: "actions",
            header: "Actions",
            render: (row) => (
              <div className="flex gap-2">
                <button type="button" className="rounded border border-slate-300 px-2 py-1 text-xs" onClick={() => openEditModal(row)}>
                  Edit
                </button>
                <button type="button" className="rounded border border-rose-200 px-2 py-1 text-xs text-rose-700" onClick={() => setDeletingReceptionistId(row.userId)}>
                  Delete
                </button>
              </div>
            ),
          },
        ]}
      />

      <Modal open={Boolean(editingReceptionist)} title="Edit Receptionist" onClose={() => setEditingReceptionist(null)}>
        <form className="grid gap-3 md:grid-cols-2" onSubmit={editForm.handleSubmit((values) => updateMutation.mutate(values))}>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="First Name" {...editForm.register("firstName")} />
            <FormErrorText message={editForm.formState.errors.firstName?.message} />
          </div>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Last Name" {...editForm.register("lastName")} />
            <FormErrorText message={editForm.formState.errors.lastName?.message} />
          </div>
          <div className="md:col-span-2">
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" {...editForm.register("phone")} />
            <FormErrorText message={editForm.formState.errors.phone?.message} />
          </div>
          <div className="md:col-span-2">
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...editForm.register("accountStatus")}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
            <FormErrorText message={editForm.formState.errors.accountStatus?.message} />
          </div>
          <div className="md:col-span-2 flex justify-end">
            <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? "Updating..." : "Save Changes"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deletingReceptionistId)}
        title="Delete Receptionist"
        description="This will permanently remove the selected receptionist user."
        confirmLabel="Delete"
        isConfirming={deleteMutation.isPending}
        onCancel={() => setDeletingReceptionistId(null)}
        onConfirm={() => {
          if (deletingReceptionistId) {
            deleteMutation.mutate(deletingReceptionistId, {
              onSettled: () => setDeletingReceptionistId(null),
            });
          }
        }}
      />
    </div>
  );
}