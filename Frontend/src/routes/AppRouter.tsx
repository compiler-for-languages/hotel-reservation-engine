import { Suspense, lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { FullPageLoader } from "@/components/common/FullPageLoader";
import { AppLayout } from "@/layouts/AppLayout";
import { AuthLayout } from "@/layouts/AuthLayout";
import { PublicOnlyRoute, ProtectedRoute, RoleRoute } from "@/routes/RouteGuards";
import { useAuthStore } from "@/store/authStore";
import { dashboardByRole } from "@/utils/role";

const LoginPage = lazy(() => import("@/pages/auth/LoginPage"));
const RegisterPage = lazy(() => import("@/pages/auth/RegisterPage"));
const CustomerDashboardPage = lazy(() => import("@/pages/customer/CustomerDashboardPage"));
const SearchRoomsPage = lazy(() => import("@/pages/customer/SearchRoomsPage"));
const MyReservationsPage = lazy(() => import("@/pages/customer/MyReservationsPage"));
const PaymentsPage = lazy(() => import("@/pages/customer/PaymentsPage"));
const BookingHistoryPage = lazy(() => import("@/pages/customer/BookingHistoryPage"));
const CustomerProfilePage = lazy(() => import("@/pages/customer/CustomerProfilePage"));
const ReceptionistDashboardPage = lazy(() => import("@/pages/receptionist/ReceptionistDashboardPage"));
const TodayArrivalsPage = lazy(() => import("@/pages/receptionist/TodayArrivalsPage"));
const TodayDeparturesPage = lazy(() => import("@/pages/receptionist/TodayDeparturesPage"));

const AssignRoomPage = lazy(() => import("@/pages/receptionist/AssignRoomPage"));
const CheckInPage = lazy(() => import("@/pages/receptionist/CheckInPage"));
const CheckOutPage = lazy(() => import("@/pages/receptionist/CheckOutPage"));
const AdminDashboardPage = lazy(() => import("@/pages/admin/AdminDashboardPage"));
const CustomersPage = lazy(() => import("@/pages/admin/CustomersPage"));
const ReceptionistsPage = lazy(() => import("@/pages/admin/ReceptionistsPage"));
const UsersPage = lazy(() => import("@/pages/admin/UsersPage"));
const RoomTypesPage = lazy(() => import("@/pages/admin/RoomTypesPage"));
const RoomsPage = lazy(() => import("@/pages/admin/RoomsPage"));
const ReservationsPage = lazy(() => import("@/pages/admin/ReservationsPage"));
const PaymentsAdminPage = lazy(() => import("@/pages/admin/PaymentsAdminPage"));
const AdminProfilePage = lazy(() => import("@/pages/admin/AdminProfilePage"));
const NotFoundPage = lazy(() => import("@/pages/shared/NotFoundPage"));

const HomeRedirect = () => {
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (isAuthenticated && user) {
    return <Navigate to={dashboardByRole[user.role]} replace />;
  }

  return <Navigate to="/login" replace />;
};

export const AppRouter = () => (
  <Suspense fallback={<FullPageLoader label="Loading page..." />}>
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route element={<RoleRoute allowedRoles={["CUSTOMER"]} />}>
            <Route path="/customer/dashboard" element={<CustomerDashboardPage />} />
            <Route path="/customer/search-rooms" element={<SearchRoomsPage />} />
            <Route path="/customer/my-reservations" element={<MyReservationsPage />} />
            <Route path="/customer/payments" element={<PaymentsPage />} />
            <Route path="/customer/booking-history" element={<BookingHistoryPage />} />
            <Route path="/customer/profile" element={<CustomerProfilePage />} />
          </Route>

          <Route element={<RoleRoute allowedRoles={["RECEPTIONIST"]} />}>
            <Route path="/receptionist/dashboard" element={<ReceptionistDashboardPage />} />
            <Route path="/receptionist/today-arrivals" element={<TodayArrivalsPage />} />
            <Route path="/receptionist/today-departures" element={<TodayDeparturesPage />} />
            <Route path="/receptionist/assign-room" element={<AssignRoomPage />} />
            <Route path="/receptionist/check-in" element={<CheckInPage />} />
            <Route path="/receptionist/check-out" element={<CheckOutPage />} />
          </Route>

          <Route element={<RoleRoute allowedRoles={["ADMIN"]} />}>
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="/admin/customers" element={<CustomersPage />} />
            <Route path="/admin/receptionists" element={<ReceptionistsPage />} />
            <Route path="/admin/users" element={<UsersPage />} />
            <Route path="/admin/room-types" element={<RoomTypesPage />} />
            <Route path="/admin/rooms" element={<RoomsPage />} />
            <Route path="/admin/reservations" element={<ReservationsPage />} />
            <Route path="/admin/payments" element={<PaymentsAdminPage />} />
            <Route path="/admin/profile" element={<AdminProfilePage />} />
          </Route>
        </Route>
      </Route>

      <Route path="/" element={<HomeRedirect />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  </Suspense>
);