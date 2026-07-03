export const formatDate = (value: string) => new Date(value).toLocaleDateString();

export const formatDateTime = (value: string) => new Date(value).toLocaleString();

export const formatCurrency = (value: number, currency = "INR") =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency, maximumFractionDigits: 2 }).format(value);

export const formatGuestNames = (guests?: { firstName: string; lastName: string }[]) => {
  if (!guests?.length) {
    return "-";
  }

  return guests.map((guest) => `${guest.firstName} ${guest.lastName}`).join(", ");
};