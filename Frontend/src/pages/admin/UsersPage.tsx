import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { DataTable } from "@/components/common/DataTable";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { FormErrorText } from "@/components/common/FormErrorText";
import { Modal } from "@/components/common/Modal";
import { StatusBadge } from "@/components/common/StatusBadge";
import { UserService } from "@/services/UserService";
import { useAuthStore } from "@/store/authStore";
import type { UserResponseDTO } from "@/types/api";
import { getApiErrorMessage } from "@/utils/http";

const userPatchSchema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  phone: z.string().min(7, "Phone is required."),
  accountStatus: z.enum(["ACTIVE", "INACTIVE"]),
});

type UserPatchFormValues = z.infer<typeof userPatchSchema>;

export default function UsersPage() {
  const queryClient = useQueryClient();
  const currentUser = useAuthStore((state) => state.user);
  const [editingUser, setEditingUser] = useState<UserResponseDTO | null>(null);
  const [deletingUserId, setDeletingUserId] = useState<number | null>(null);
  const [lookupUserId, setLookupUserId] = useState("");

  const editForm = useForm<UserPatchFormValues>({
    resolver: zodResolver(userPatchSchema),
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
    queryKey: ["adminUsers"],
    queryFn: UserService.getAllUsers,
  });

  const deactivateMutation = useMutation({
    mutationFn: (userId: number) => {
      // Frontend guard: admin cannot deactivate their own account
      if (currentUser && currentUser.userId === userId && currentUser.role === "ADMIN") {
        throw new Error("You cannot deactivate your own administrator account.");
      }
      const user = data.find((item) => item.userId === userId);
      if (!user) {
        throw new Error("User not found.");
      }
      return UserService.updateUser(userId, {
        firstName: user.firstName,
        lastName: user.lastName,
        phone: user.phone,
        accountStatus: user.accountStatus === "ACTIVE" ? "INACTIVE" : "ACTIVE",
      });
    },
    onSuccess: () => {
      toast.success("User status updated.");
      void queryClient.invalidateQueries({ queryKey: ["adminUsers"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update user status."));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (userId: number) => UserService.deleteUser(userId),
    onSuccess: () => {
      toast.success("User deleted successfully.");
      void queryClient.invalidateQueries({ queryKey: ["adminUsers"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to delete user."));
    },
  });

  const editMutation = useMutation({
    mutationFn: (payload: UserPatchFormValues) => {
      if (!editingUser) {
        throw new Error("No user selected.");
      }
      return UserService.updateUser(editingUser.userId, payload);
    },
    onSuccess: () => {
      toast.success("User updated successfully.");
      setEditingUser(null);
      void queryClient.invalidateQueries({ queryKey: ["adminUsers"] });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update user."));
    },
  });

  const openEditModal = (user: UserResponseDTO) => {
    setEditingUser(user);
    editForm.reset({
      firstName: user.firstName,
      lastName: user.lastName,
      phone: user.phone,
      accountStatus: user.accountStatus,
    });
  };

  const lookupMutation = useMutation({
    mutationFn: (userId: number) => UserService.getUserById(userId),
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Unable to find user by ID."));
    },
  });

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-semibold text-slate-900">Users</h2>

      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-900">Lookup User By ID</h3>
        <div className="mt-2 flex flex-wrap gap-2">
          <input
            value={lookupUserId}
            onChange={(event) => setLookupUserId(event.target.value)}
            type="number"
            placeholder="Enter numeric user ID (e.g. 5)"
            className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          />
          <button
            type="button"
            onClick={() => {
              if (!lookupUserId) {
                toast.error("User ID is required.");
                return;
              }
              lookupMutation.mutate(Number(lookupUserId));
            }}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white"
            disabled={lookupMutation.isPending}
          >
            {lookupMutation.isPending ? "Searching..." : "Find"}
          </button>
        </div>

        {lookupMutation.data ? (
          <div className="mt-3 rounded border border-slate-200 p-3 text-sm text-slate-700">
            <p>ID: {lookupMutation.data.userId}</p>
            <p>Name: {lookupMutation.data.firstName} {lookupMutation.data.lastName}</p>
            <p>Email: {lookupMutation.data.email}</p>
            <p>Phone: {lookupMutation.data.phone}</p>
            <p>Role: {lookupMutation.data.role}</p>
          </div>
        ) : null}
      </div>

      <DataTable
        title="All Users"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load users." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No users available yet."
        rowKey={(row) => row.userId}
        searchPlaceholder="Search by name, email, role"
        searchTextExtractor={(row) => `${row.firstName} ${row.lastName} ${row.email} ${row.role}`}
        columns={[
          { key: "id", header: "ID", render: (row) => row.userId, sortValue: (row) => row.userId },
          { key: "name", header: "Name", render: (row) => `${row.firstName} ${row.lastName}`, sortValue: (row) => `${row.firstName} ${row.lastName}` },
          { key: "email", header: "Email", render: (row) => row.email, sortValue: (row) => row.email },
          { key: "role", header: "Role", render: (row) => row.role, sortValue: (row) => row.role },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.accountStatus} />, sortValue: (row) => row.accountStatus },
          {
            key: "actions",
            header: "Actions",
            render: (row) => {
              const isSelf = currentUser?.userId === row.userId && currentUser?.role === "ADMIN";
              const selfTooltip = "You cannot modify your own administrator account.";
              const busyDisabled = deactivateMutation.isPending || editMutation.isPending || deleteMutation.isPending;
              return (
                <div className="flex gap-2">
                  <button
                    type="button"
                    className={`rounded border px-2 py-1 text-xs transition ${
                      isSelf
                        ? "cursor-not-allowed border-slate-200 bg-slate-100 text-slate-400"
                        : "border-slate-300 hover:bg-slate-50"
                    }`}
                    onClick={() => {
                      if (isSelf) {
                        toast.error("You cannot deactivate your own administrator account.");
                        return;
                      }
                      deactivateMutation.mutate(row.userId);
                    }}
                    disabled={isSelf || busyDisabled}
                    title={isSelf ? selfTooltip : "Toggle account status"}
                  >
                    Toggle Status
                  </button>
                  <button
                    type="button"
                    className="rounded border border-slate-300 px-2 py-1 text-xs"
                    onClick={() => openEditModal(row)}
                    disabled={busyDisabled}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    className={`rounded border px-2 py-1 text-xs transition ${
                      isSelf
                        ? "cursor-not-allowed border-rose-100 bg-rose-50 text-rose-300"
                        : "border-rose-200 text-rose-700 hover:bg-rose-50"
                    }`}
                    onClick={() => {
                      if (isSelf) {
                        toast.error("You cannot delete your own administrator account.");
                        return;
                      }
                      setDeletingUserId(row.userId);
                    }}
                    disabled={isSelf || busyDisabled}
                    title={isSelf ? selfTooltip : "Delete user"}
                  >
                    Delete
                  </button>
                </div>
              );
            },
          },
        ]}
      />

      <Modal open={Boolean(editingUser)} title="Edit User" onClose={() => setEditingUser(null)}>
        <form className="grid gap-3 md:grid-cols-2" onSubmit={editForm.handleSubmit((values) => editMutation.mutate(values))}>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="User first name" {...editForm.register("firstName")} />
            <FormErrorText message={editForm.formState.errors.firstName?.message} />
          </div>
          <div>
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="User last name" {...editForm.register("lastName")} />
            <FormErrorText message={editForm.formState.errors.lastName?.message} />
          </div>
          <div className="md:col-span-2">
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="10-digit mobile number" {...editForm.register("phone")} />
            <FormErrorText message={editForm.formState.errors.phone?.message} />
          </div>
          <div className="md:col-span-2">
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" title="ACTIVE allows login, INACTIVE blocks access" {...editForm.register("accountStatus")}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
            <FormErrorText message={editForm.formState.errors.accountStatus?.message} />
          </div>
          <div className="md:col-span-2 flex justify-end">
            <button type="submit" className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white" disabled={editMutation.isPending}>
              {editMutation.isPending ? "Updating..." : "Save Changes"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deletingUserId)}
        title="Delete User"
        description="This action will permanently remove the selected user. This cannot be undone."
        confirmLabel="Delete"
        isConfirming={deleteMutation.isPending}
        onCancel={() => setDeletingUserId(null)}
        onConfirm={() => {
          if (deletingUserId) {
            deleteMutation.mutate(deletingUserId, {
              onSettled: () => setDeletingUserId(null),
            });
          }
        }}
      />
    </div>
  );
}