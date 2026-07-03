import type { Gender } from "@/types/api";

export interface OptionalGuestEntry {
  firstName: string;
  lastName: string;
  gender: Gender;
  dateOfBirth: string;
  phone: string;
}

export const createEmptyGuestEntry = (): OptionalGuestEntry => ({
  firstName: "",
  lastName: "",
  gender: "MALE",
  dateOfBirth: "",
  phone: "",
});

export const isGuestEntryActive = (entry: OptionalGuestEntry) =>
  [entry.firstName, entry.lastName, entry.phone, entry.dateOfBirth].some((value) => value.trim().length > 0);

export const getActiveGuestEntries = (entries: OptionalGuestEntry[]) => entries.filter(isGuestEntryActive);

export const validateOptionalGuestEntries = (entries: OptionalGuestEntry[], guestCount: number): string | null => {
  const activeEntries = getActiveGuestEntries(entries);

  if (activeEntries.length > guestCount) {
    return "Guest details cannot exceed the selected guest count.";
  }

  for (const entry of activeEntries) {
    if (!entry.firstName.trim()) {
      return "First name is required for each guest entry you add.";
    }
    if (!entry.lastName.trim()) {
      return "Last name is required for each guest entry you add.";
    }
    if (!entry.dateOfBirth) {
      return "Date of birth is required for each guest entry you add.";
    }
    if (entry.phone.trim() && entry.phone.trim().length < 7) {
      return "Mobile number must be at least 7 digits when provided.";
    }
  }

  return null;
};
