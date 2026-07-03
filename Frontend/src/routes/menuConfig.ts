import type { Role } from "@/types/api";

export interface MenuItem {
  label: string;
  path: string;
}

export const menuByRole: Record<Role, MenuItem[]> = {
  CUSTOMER: [
    { label: "Dashboard", path: "/customer/dashboard" },
    { label: "Search Rooms", path: "/customer/search-rooms" },
    { label: "My Reservations", path: "/customer/my-reservations" },
    { label: "Payments", path: "/customer/payments" },
    { label: "Booking History", path: "/customer/booking-history" },
    { label: "Profile", path: "/customer/profile" },
  ],
  RECEPTIONIST: [
    { label: "Dashboard", path: "/receptionist/dashboard" },
    { label: "Today's Arrivals", path: "/receptionist/today-arrivals" },
    { label: "Today's Departures", path: "/receptionist/today-departures" },
    { label: "Current Guests", path: "/receptionist/current-guests" },

    { label: "Assign Room", path: "/receptionist/assign-room" },
    { label: "Check In", path: "/receptionist/check-in" },
    { label: "Check Out", path: "/receptionist/check-out" },
  ],
  ADMIN: [
    { label: "Dashboard", path: "/admin/dashboard" },
    { label: "Customers", path: "/admin/customers" },
    { label: "Receptionists", path: "/admin/receptionists" },
    { label: "Users", path: "/admin/users" },
    { label: "Room Types", path: "/admin/room-types" },
    { label: "Rooms", path: "/admin/rooms" },
    { label: "Reservations", path: "/admin/reservations" },
    { label: "Payments", path: "/admin/payments" },
    { label: "Profile", path: "/admin/profile" },
  ],
};