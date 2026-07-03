export const formatDate = (value: string) => new Date(value).toLocaleDateString();

export const formatDateTime = (value: string) => new Date(value).toLocaleString();

export const formatCurrency = (value: number | string, currency = "INR") => {
  const amount = typeof value === "string" ? Number(value) : value;
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(Number.isFinite(amount) ? amount : 0);
};

export const formatGuestNames = (guests?: { firstName: string; lastName: string }[]) => {
  if (!guests?.length) {
    return "-";
  }

  return guests.map((guest) => `${guest.firstName} ${guest.lastName}`).join(", ");
};