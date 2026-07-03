import { FormErrorText } from "@/components/common/FormErrorText";
import type { OptionalGuestEntry } from "@/utils/guest";
import type { Gender } from "@/types/api";
import { secondaryButtonClass, tableDangerButtonClass } from "@/utils/ui";

interface OptionalGuestDetailsSectionProps {
  guestCount: number;
  entries: OptionalGuestEntry[];
  fieldErrors?: Record<number, Partial<Record<keyof OptionalGuestEntry, string>>>;
  formError?: string | null;
  onAddGuest: () => void;
  onRemoveGuest: (index: number) => void;
  onChangeGuest: (index: number, field: keyof OptionalGuestEntry, value: string) => void;
}

const genderOptions: Gender[] = ["MALE", "FEMALE", "OTHER"];

export const OptionalGuestDetailsSection = ({
  guestCount,
  entries,
  fieldErrors = {},
  formError,
  onAddGuest,
  onRemoveGuest,
  onChangeGuest,
}: OptionalGuestDetailsSectionProps) => (
  <div className="md:col-span-4 space-y-3 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4">
    <div>
      <h4 className="text-base font-semibold text-slate-900">Guest Details (Optional)</h4>
      <p className="mt-1 text-sm text-slate-600">
        You may skip this section and continue booking, or add guest information now. Reception can complete guest details later if needed.
      </p>
    </div>

    {entries.length === 0 ? (
      <p className="text-sm text-slate-500">No guest entries added yet.</p>
    ) : (
      <div className="space-y-3">
        {entries.map((entry, index) => (
          <div key={`guest-entry-${index}`} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <div className="mb-3 flex items-center justify-between gap-3">
              <p className="text-sm font-medium text-slate-800">Guest {index + 1}</p>
              <button type="button" className={tableDangerButtonClass} onClick={() => onRemoveGuest(index)}>
                Remove
              </button>
            </div>

            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">First Name</label>
                <input
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                  value={entry.firstName}
                  onChange={(event) => onChangeGuest(index, "firstName", event.target.value)}
                />
                <FormErrorText message={fieldErrors[index]?.firstName} />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Last Name</label>
                <input
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                  value={entry.lastName}
                  onChange={(event) => onChangeGuest(index, "lastName", event.target.value)}
                />
                <FormErrorText message={fieldErrors[index]?.lastName} />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Gender</label>
                <select
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                  value={entry.gender}
                  onChange={(event) => onChangeGuest(index, "gender", event.target.value)}
                >
                  {genderOptions.map((gender) => (
                    <option key={gender} value={gender}>
                      {gender}
                    </option>
                  ))}
                </select>
                <FormErrorText message={fieldErrors[index]?.gender} />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Date of Birth</label>
                <input
                  type="date"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                  value={entry.dateOfBirth}
                  onChange={(event) => onChangeGuest(index, "dateOfBirth", event.target.value)}
                />
                <FormErrorText message={fieldErrors[index]?.dateOfBirth} />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Mobile Number (Optional)</label>
                <input
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                  value={entry.phone}
                  onChange={(event) => onChangeGuest(index, "phone", event.target.value)}
                />
                <FormErrorText message={fieldErrors[index]?.phone} />
              </div>
            </div>
          </div>
        ))}
      </div>
    )}

    <div className="flex flex-wrap items-center gap-3">
      <button
        type="button"
        className={secondaryButtonClass}
        onClick={onAddGuest}
        disabled={entries.length >= guestCount}
      >
        Add Guest
      </button>
      <p className="text-xs text-slate-500">
        Up to {guestCount} guest {guestCount === 1 ? "entry" : "entries"} based on your selected guest count.
      </p>
    </div>

    <FormErrorText message={formError} />
  </div>
);
