import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { z } from "zod";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { FormErrorText } from "@/components/common/FormErrorText";
import { ReceptionService } from "@/services/ReceptionService";
import { StatusBadge } from "@/components/common/StatusBadge";
import { getApiErrorMessage } from "@/utils/http";

const schema = z.object({
  reservationId: z.string().min(1, "Reservation ID is required."),
});

interface FormValues {
  reservationId: string;
}

export default function AssignRoomPage() {
  const [pendingValues, setPendingValues] = useState<FormValues | null>(null);
  const { register, handleSubmit, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { reservationId: "" },
  });

  const mutation = useMutation({
    mutationFn: (payload: FormValues) => ReceptionService.assignRoom({ reservationId: Number(payload.reservationId) }),
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to assign room."));
    },
  });

  return (
    <div className="space-y-5">
      <h2 className="text-2xl font-semibold text-slate-900">Assign Room</h2>

      <form onSubmit={handleSubmit((values) => setPendingValues(values))} className="max-w-lg space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Reservation ID</label>
          <input type="number" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("reservationId")} />
          <FormErrorText message={errors.reservationId?.message} />
        </div>

        <button type="button" onClick={handleSubmit((values) => setPendingValues(values))} className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white" disabled={mutation.isPending}>
          {mutation.isPending ? "Assigning..." : "Assign Room"}
        </button>
      </form>

      {mutation.data ? (
        <div className="rounded-lg border border-slate-200 bg-white p-4 text-sm shadow-sm">
          <p>Assignment ID: {mutation.data.assignmentId}</p>
          <p>Customer: {mutation.data.customerName}</p>
          <p>Room: {mutation.data.roomNumber}</p>
          <div className="mt-2">
            <StatusBadge value={mutation.data.assignmentStatus} />
          </div>
        </div>
      ) : null}

      <ConfirmDialog
        open={Boolean(pendingValues)}
        title="Assign Room"
        description="Proceed to assign room for this reservation?"
        confirmLabel="Assign"
        isConfirming={mutation.isPending}
        onCancel={() => setPendingValues(null)}
        onConfirm={() => {
          if (pendingValues) {
            mutation.mutate(pendingValues, {
              onSettled: () => setPendingValues(null),
            });
          }
        }}
      />
    </div>
  );
}