import { zodResolver } from "@hookform/resolvers/zod";
import { AxiosError } from "axios";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { Link, useNavigate } from "react-router-dom";
import { z } from "zod";
import { Spinner } from "@/components/common/Spinner";
import { PasswordInput } from "@/components/common/PasswordInput";
import { useRegister } from "@/hooks/useAuthActions";
import type { Gender } from "@/types/api";

const registerSchema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  gender: z.enum(["MALE", "FEMALE", "OTHER"]),
  email: z.string().email("Enter a valid email."),
  phone: z.string().min(7, "Phone number is required."),
  password: z.string().min(6, "Password must be at least 6 characters."),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

const genderOptions: Gender[] = ["MALE", "FEMALE", "OTHER"];

export default function RegisterPage() {
  const navigate = useNavigate();
  const registerMutation = useRegister();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      gender: "MALE",
      email: "",
      phone: "",
      password: "",
    },
  });

  const onSubmit = async (values: RegisterFormValues) => {
    try {
      await registerMutation.mutateAsync(values);
      navigate("/login", { replace: true });
    } catch (error) {
      const message =
        error instanceof AxiosError
          ? (error.response?.data?.message ?? "Unable to register. Please verify the details.")
          : "Unable to register. Please try again.";
      toast.error(message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-1 text-center">
        <h1 className="text-2xl font-semibold text-slate-900">Create Customer Account</h1>
        <p className="text-sm text-slate-600">Register to start booking rooms</p>
      </div>

      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="firstName" className="mb-1 block text-sm font-medium text-slate-700">
              First Name
            </label>
            <input
              id="firstName"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-slate-500"
              placeholder="Enter your first name (e.g. Rahul)"
              {...register("firstName")}
            />
            {errors.firstName ? <p className="mt-1 text-xs text-rose-600">{errors.firstName.message}</p> : null}
          </div>

          <div>
            <label htmlFor="lastName" className="mb-1 block text-sm font-medium text-slate-700">
              Last Name
            </label>
            <input
              id="lastName"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-slate-500"
              placeholder="Enter your last name (e.g. Sharma)"
              {...register("lastName")}
            />
            {errors.lastName ? <p className="mt-1 text-xs text-rose-600">{errors.lastName.message}</p> : null}
          </div>
        </div>

        <div>
          <label htmlFor="gender" className="mb-1 block text-sm font-medium text-slate-700">
            Gender
          </label>
          <select
            id="gender"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-slate-500"
            {...register("gender")}
          >
            {genderOptions.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-700">
            Email
          </label>
          <input
            id="email"
            type="email"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-slate-500"
            placeholder="Enter email address (e.g. you@example.com)"
            {...register("email")}
          />
          {errors.email ? <p className="mt-1 text-xs text-rose-600">{errors.email.message}</p> : null}
        </div>

        <div>
          <label htmlFor="phone" className="mb-1 block text-sm font-medium text-slate-700">
            Phone
          </label>
          <input
            id="phone"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-slate-500"
            placeholder="Enter 10-digit mobile number (e.g. 9876543210)"
            {...register("phone")}
          />
          {errors.phone ? <p className="mt-1 text-xs text-rose-600">{errors.phone.message}</p> : null}
        </div>

        <div>
          <label htmlFor="password" className="mb-1 block text-sm font-medium text-slate-700">
            Password
          </label>
          <PasswordInput
            id="password"
            placeholder="Create a password (minimum 6 characters)"
            {...register("password")}
          />
          {errors.password ? <p className="mt-1 text-xs text-rose-600">{errors.password.message}</p> : null}
        </div>

        <button
          type="submit"
          disabled={registerMutation.isPending}
          className="flex h-10 w-full items-center justify-center gap-2 rounded-md bg-slate-900 text-sm font-medium text-white transition hover:bg-slate-800 disabled:opacity-70"
        >
          {registerMutation.isPending ? <Spinner /> : null}
          Register
        </button>
      </form>

      <p className="text-center text-sm text-slate-600">
        Already have an account?{" "}
        <Link to="/login" className="font-medium text-slate-900 underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}