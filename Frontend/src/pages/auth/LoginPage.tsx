import { zodResolver } from "@hookform/resolvers/zod";
import { AxiosError } from "axios";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { Link, useNavigate } from "react-router-dom";
import { z } from "zod";
import { Spinner } from "@/components/common/Spinner";
import { PasswordInput } from "@/components/common/PasswordInput";
import { useLogin } from "@/hooks/useAuthActions";
import { dashboardByRole } from "@/utils/role";

const loginSchema = z.object({
  email: z.string().email("Enter a valid email."),
  password: z.string().min(1, "Password is required."),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const navigate = useNavigate();
  const loginMutation = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = async (values: LoginFormValues) => {
    try {
      const user = await loginMutation.mutateAsync(values);
      toast.success("Logged in successfully.");
      navigate(dashboardByRole[user.role], { replace: true });
    } catch (error) {
      const message =
        error instanceof AxiosError
          ? (error.response?.data?.message ?? "Invalid credentials or account is unavailable.")
          : "Unable to login. Please try again.";
      toast.error(message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-1 text-center">
        <h1 className="text-2xl font-semibold text-slate-900">Hotel Reservation Engine</h1>
        <p className="text-sm text-slate-600">Sign in to continue</p>
      </div>

      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div>
          <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-700">
            Email
          </label>
          <input
            id="email"
            type="email"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-slate-500"
            placeholder="you@example.com"
            {...register("email")}
          />
          {errors.email ? <p className="mt-1 text-xs text-rose-600">{errors.email.message}</p> : null}
        </div>

        <div>
          <label htmlFor="password" className="mb-1 block text-sm font-medium text-slate-700">
            Password
          </label>
          <PasswordInput
            id="password"
            placeholder="Enter your password"
            {...register("password")}
          />
          {errors.password ? <p className="mt-1 text-xs text-rose-600">{errors.password.message}</p> : null}
        </div>

        <button
          type="submit"
          disabled={loginMutation.isPending}
          className="flex h-10 w-full items-center justify-center gap-2 rounded-md bg-slate-900 text-sm font-medium text-white transition hover:bg-slate-800 disabled:opacity-70"
        >
          {loginMutation.isPending ? <Spinner /> : null}
          Login
        </button>
      </form>

      <p className="text-center text-sm text-slate-600">
        New customer?{" "}
        <Link to="/register" className="font-medium text-slate-900 underline">
          Create an account
        </Link>
      </p>
    </div>
  );
}