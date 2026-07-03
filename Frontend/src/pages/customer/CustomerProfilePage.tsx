import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { z } from "zod";
import { PageHeader } from "@/components/common/PageHeader";
import { AuthService } from "@/services/AuthService";
import { UserService } from "@/services/UserService";
import { useAuthStore } from "@/store/authStore";
import { getApiErrorMessage } from "@/utils/http";
import { primaryButtonClass } from "@/utils/ui";

const schema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  phone: z.string().min(7, "Mobile number is required."),
});

type ProfileFormValues = z.infer<typeof schema>;

export default function CustomerProfilePage() {
  const user = useAuthStore((state) => state.user);
  const setSession = useAuthStore((state) => state.setSession);
  const token = useAuthStore((state) => state.token);

  const { data: profile } = useQuery({
    queryKey: ["currentUser", user?.userId],
    queryFn: AuthService.getCurrentUser,
    enabled: Boolean(token),
  });

  useEffect(() => {
    if (profile && token) {
      setSession({ token, user: profile });
    }
  }, [profile, setSession, token]);

  const activeUser = profile ?? user;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(schema),
    values: {
      firstName: activeUser?.firstName ?? "",
      lastName: activeUser?.lastName ?? "",
      phone: activeUser?.phone ?? "",
    },
  });

  const updateMutation = useMutation({
    mutationFn: async (payload: ProfileFormValues) => {
      if (!activeUser) {
        throw new Error("No active user session.");
      }

      return UserService.updateUser(activeUser.userId, {
        firstName: payload.firstName,
        lastName: payload.lastName,
        phone: payload.phone,
        accountStatus: activeUser.accountStatus,
      });
    },
    onSuccess: (updatedUser) => {
      if (token) {
        setSession({ token, user: updatedUser });
      }
      toast.success("Profile updated successfully.");
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, "Failed to update profile."));
    },
  });

  const onSubmit = async (values: ProfileFormValues) => {
    await updateMutation.mutateAsync(values);
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Profile" description="Manage your personal information." />

      <form className="max-w-xl space-y-4 rounded-lg border border-slate-200 bg-white p-4 shadow-sm" onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
          <input className="w-full rounded-md border border-slate-300 bg-slate-100 px-3 py-2 text-sm" value={activeUser?.email ?? ""} disabled />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">First Name</label>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("firstName")} />
          {errors.firstName ? <p className="mt-1 text-xs text-rose-600">{errors.firstName.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Last Name</label>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("lastName")} />
          {errors.lastName ? <p className="mt-1 text-xs text-rose-600">{errors.lastName.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Mobile Number</label>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...register("phone")} />
          {errors.phone ? <p className="mt-1 text-xs text-rose-600">{errors.phone.message}</p> : null}
        </div>

        <button type="submit" className={primaryButtonClass} disabled={updateMutation.isPending}>
          {updateMutation.isPending ? "Saving..." : "Save Changes"}
        </button>
      </form>
    </div>
  );
}
