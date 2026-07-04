import { useLocation } from "react-router-dom";
import { FeaturePlaceholderPage } from "@/pages/shared/FeaturePlaceholderPage";

const pageMeta: Record<string, { title: string; description: string }> = {
  "/customer/search-rooms": {
    title: "Search Rooms",
    description: "Room search and availability experience will be integrated with /api/availability/search in Module 2.",
  },
  "/customer/my-reservations": {
    title: "My Reservations",
    description: "Reservation management will be integrated with reservation APIs in Module 2.",
  },
  "/customer/payments": {
    title: "Payments",
    description: "Payment execution and status tracking will be integrated in Module 2.",
  },
  "/customer/booking-history": {
    title: "Booking History",
    description: "Historical reservation and payment records will be connected in Module 2.",
  },
  "/customer/profile": {
    title: "Profile",
    description: "Customer profile management will be connected to user APIs in Module 2.",
  },
  "/receptionist/today-arrivals": {
    title: "Today's Arrivals",
    description: "This section will consume /api/reception/today-arrivals in Module 2.",
  },
  "/receptionist/today-departures": {
    title: "Today's Departures",
    description: "This section will consume /api/reception/today-departures in Module 2.",
  },
  "/receptionist/assign-room": {
    title: "Assign Room",
    description: "Room assignment flow using /api/reception/assign-room will be added in Module 2.",
  },
  "/receptionist/check-in": {
    title: "Check In",
    description: "Check-in flow using /api/reception/check-in will be added in Module 2.",
  },
  "/receptionist/check-out": {
    title: "Check Out",
    description: "Check-out flow using /api/reception/check-out will be added in Module 2.",
  },
  "/admin/customers": {
    title: "Customers",
    description: "Customer administration with /api/users/customers will be added in Module 2.",
  },
  "/admin/receptionists": {
    title: "Receptionists",
    description: "Receptionist administration with /api/users/receptionists and /api/users/receptionist will be added in Module 2.",
  },
  "/admin/users": {
    title: "Users",
    description: "Global user management using admin user endpoints will be added in Module 2.",
  },
  "/admin/room-types": {
    title: "Room Types",
    description: "Room type management using /api/roomtype endpoints will be added in Module 2.",
  },
  "/admin/rooms": {
    title: "Rooms",
    description: "Room inventory management with /api/room endpoints will be added in Module 2.",
  },
  "/admin/reservations": {
    title: "Reservations",
    description: "Reservation administration with /api/reservation endpoints will be added in Module 2.",
  },
  "/admin/payments": {
    title: "Payments",
    description: "Payment administration with /api/payment endpoints will be added in Module 2.",
  },
  "/admin/profile": {
    title: "Profile",
    description: "Admin profile management will be added in Module 2.",
  },
};

export default function RoleFeaturePage() {
  const location = useLocation();
  const meta = pageMeta[location.pathname] ?? {
    title: "Feature",
    description: "This page will be implemented in the next module.",
  };

  return <FeaturePlaceholderPage title={meta.title} description={meta.description} />;
}