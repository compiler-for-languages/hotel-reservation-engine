export type Gender = "MALE" | "FEMALE" | "OTHER";

export type Role = "CUSTOMER" | "ADMIN" | "RECEPTIONIST";

export type AccountStatus = "ACTIVE" | "INACTIVE";

export type RoomStatus = "AVAILABLE" | "OCCUPIED" | "MAINTENANCE" | "OUT_OF_SERVICE";

export type ReservationStatus =
  | "PENDING"
  | "CONFIRMED"
  | "CHECKED_IN"
  | "CHECKED_OUT"
  | "CANCELLED"
  | "EXPIRED";

export type AssignmentStatus = "ASSIGNED" | "CHECKED_IN" | "CHECKED_OUT";

export type PaymentMethod = "UPI" | "CARD" | "NET_BANKING" | "WALLET";

export type PaymentStatus = "PENDING" | "PROCESSING" | "SUCCESS" | "FAILED" | "REFUNDED";

export interface UserRequestDTO {
  firstName: string;
  lastName: string;
  gender: Gender;
  email: string;
  phone: string;
  password: string;
  role: Role;
  accountStatus: AccountStatus;
}

export interface UserResponseDTO {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: Role;
  accountStatus: AccountStatus;
}

export interface RoomTypeRequestDTO {
  name: string;
  description: string;
  pricePerNight: number;
  capacity: number;
  status: AccountStatus;
}

export interface RoomTypeResponseDTO {
  roomTypeId: number;
  name: string;
  description: string;
  pricePerNight: number;
  capacity: number;
  status: AccountStatus;
}

export interface RoomRequestDTO {
  roomNumber: string;
  roomTypeId: number;
  floorNumber: number;
  roomStatus: RoomStatus;
}

export interface RoomResponseDTO {
  roomId: number;
  roomNumber: string;
  roomTypeName: string;
  floorNumber: number;
  roomStatus: RoomStatus;
}

export interface ReservationRequestDTO {
  userId: number;
  roomTypeId: number;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  specialRequest: string;
  paymentMethod: PaymentMethod;
}

export interface ReservationResponseDTO {
  reservationId: number;
  userName: string;
  roomTypeName: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: number;
  reservationStatus: ReservationStatus;
  bookingTime: string;
  specialRequest: string;
  guests?: GuestResponseDTO[];
}

export interface AssignRoomRequestDTO {
  reservationId: number;
}

export interface RoomAssignmentResponseDTO {
  assignmentId: number;
  reservationId: number;
  customerName: string;
  roomNumber: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  actualCheckIn: string;
  actualCheckOut: string;
  assignedAt: string;
  assignmentStatus: AssignmentStatus;
}

export interface PaymentRequestDTO {
  reservationId: number;
  paymentMethod: PaymentMethod;
}

export interface PaymentResponseDTO {
  paymentId: number;
  reservationId: number;
  amount: number | string;
  currency: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  gatewayOrderId: string;
  gatewayPaymentId: string;
  gatewaySignature: string;
  paidAt: string;
}

export interface GuestRequestDTO {
  reservationId: number;
  firstName: string;
  lastName: string;
  phone: string;
  gender: Gender;
  dateOfBirth: string;
}

export interface GuestResponseDTO {
  guestId: number;
  reservationId: number;
  firstName: string;
  lastName: string;
  phone: string;
  gender: Gender;
  dateOfBirth: string;
}

export interface AvailabilityRequestDTO {
  roomTypeId: number;
  checkInDate: string;
  checkOutDate: string;
}

export interface AvailabilityCustomerResponseDTO {
  roomTypeId: number;
  roomTypeName: string;
  capacity: number;
  pricePerNight: number;
  availableRooms: number;
  available: boolean;
  availabilityMessage: string;
}

export interface AvailabilityResponseDTO {
  roomTypeId: number;
  roomTypeName: string;
  totalRooms: number;
  bookedRooms: number;
  activeHolds: number;
  availableRooms: number;
  available: boolean;
}

export interface RegisterRequestDTO {
  firstName: string;
  lastName: string;
  gender: Gender;
  email: string;
  phone: string;
  password: string;
}

export interface LoginRequestDTO {
  email: string;
  password: string;
}

export interface LoginResponseDTO {
  token: string;
}

export interface UserPatchRequestDTO {
  firstName: string;
  lastName: string;
  phone: string;
  accountStatus: AccountStatus;
}

export interface ProfilePatchRequestDTO {
  firstName: string;
  lastName: string;
  phone: string;
}

export interface RoomPatchRequestDTO {
  roomNumber: string;
  roomStatus: RoomStatus;
}

export interface ReservationPatchRequestDTO {
  reservationStatus: ReservationStatus;
  specialRequest: string;
  guestCount: number;
}

export interface GuestPatchRequestDTO {
  firstName: string;
  lastName: string;
  phone: string;
  gender: Gender;
  dateOfBirth: string;
}

export interface TodayDepartureResponseDTO {
  reservationId: number;
  customerName: string;
  roomNumber: string;
  roomType: string;
  guestCount: number;
  guestNames?: string;
  checkOutDate: string;
  actualCheckIn: string;
}

export interface TodayArrivalResponseDTO {
  reservationId: number;
  customerName: string;
  phone: string;
  roomType: string;
  guestCount: number;
  guestNames?: string;
  checkInDate: string;
  checkOutDate: string;
  roomAssigned: boolean;
  assignmentStatus: AssignmentStatus;
}

export interface ReceptionDashboardResponseDTO {
  todayArrivals: number;
  todayDepartures: number;
  currentGuests: number;
  availableRooms: number;
  occupiedRooms: number;
}

export interface GuestInfoResponseDTO {
  guestId: number;
  firstName: string;
  lastName: string;
  phone: string;
  gender: Gender;
  dateOfBirth: string;
}

export interface CurrentGuestResponseDTO {
  reservationId: number;
  primaryCustomerName: string;
  roomNumber: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  actualCheckIn: string;
  guests: GuestInfoResponseDTO[];
}