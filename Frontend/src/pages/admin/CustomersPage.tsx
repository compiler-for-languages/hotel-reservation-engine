import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { z } from "zod";
import { DataTable } from "@/components/common/DataTable";
import { PageHeader } from "@/components/common/PageHeader";
import { FormErrorText } from "@/components/common/FormErrorText";
import { Modal } from "@/components/common/Modal";
import { StatusBadge } from "@/components/common/StatusBadge";
import { UserService } from "@/services/UserService";
import type { UserResponseDTO } from "@/types/api";
import { getApiErrorMessage } from "@/utils/http";
import { tableActionButtonClass } from "@/utils/ui";

const customerPatchSchema = z.object({
  firstName: z.string().min(1),
  lastName: z.string().min(1),
  phone: z.string().min(7),
  accountStatus: z.enum(["ACTIVE", "INACTIVE"]),
});

type CustomerPatchFormValues = z.infer<typeof customerPatchSchema>;

export default function CustomersPage() {
  const queryClient = useQueryClient();
  const [editingCustomer, setEditingCustomer] = useState<UserResponseDTO | null>(null);

  const editForm = useForm<CustomerPatchFormValues>({
    resolver: zodResolver(customerPatchSchema),
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
    queryKey: ["adminCustomers"],
    queryFn: UserService.getAllCustomers,
  });

  const updateMutation = useMutation({
    mutationFn: (payload: CustomerPatchFormValues) => {
      if (!editingCustomer) {
        throw new Error("No customer selected.");
      }
      return UserService.updateUser(editingCustomer.userId, payload);
    },
    onSuccess: () => {
      toast.success("Customer updated successfully.");
      setEditingCustomer(null);
      void queryClient.invalidateQueries({ queryKey: ["adminCustomers"] });
    },
    onError: (error) => toast.error(getApiErrorMessage(error, "Failed to update customer.")),
  });

  const openEditModal = (customer: UserResponseDTO) => {
    setEditingCustomer(customer);
    editForm.reset({
      firstName: customer.firstName,
      lastName: customer.lastName,
      phone: customer.phone,
      accountStatus: customer.accountStatus,
    });
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Customers" description="Review and manage registered customer accounts." />

      <DataTable
        title="Customer List"
        data={data}
        isLoading={isLoading}
        errorMessage={error ? "Unable to load customers." : null}
        onRetry={() => {
          void refetch();
        }}
        emptyMessage="No customers available yet."
        rowKey={(row) => row.userId}
        searchPlaceholder="Search by name, email, phone"
        searchTextExtractor={(row) => `${row.firstName} ${row.lastName} ${row.email} ${row.phone}`}
        columns={[
          { key: "userId", header: "ID", render: (row) => row.userId, sortValue: (row) => row.userId },
          { key: "name", header: "Name", render: (row) => `${row.firstName} ${row.lastName}`, sortValue: (row) => `${row.firstName} ${row.lastName}` },
          { key: "email", header: "Email", render: (row) => row.email, sortValue: (row) => row.email },
          { key: "phone", header: "Mobile Number", render: (row) => row.phone, sortValue: (row) => row.phone },
          { key: "role", header: "Role", render: (row) => row.role, sortValue: (row) => row.role },
          { key: "status", header: "Status", render: (row) => <StatusBadge value={row.accountStatus} />, sortValue: (row) => row.accountStatus },
          {
            key: "actions",
            header: "Actions",
            render: (row) => (
              <button type="button" className={tableActionButtonClass} onClick={() => openEditModal(row)} disabled={updateMutation.isPending}>
                Edit
              </button>
            ),
          },
        ]}
      />

      <Modal open={Boolean(editingCustomer)} title="Edit Customer" onClose={() => setEditingCustomer(null)}>
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
    </div>
  );
}