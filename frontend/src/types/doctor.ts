export interface Doctor {
  id: string;
  firstName: string;
  lastName: string;
  specialty: string;
  email: string;
  phoneNumber: string;
  bio: string | null;
  active: boolean;
  workingHoursStart: string;
  workingHoursEnd: string;
  workingDays: string[];
}

export interface DoctorRequest {
  firstName: string;
  lastName: string;
  specialty: string;
  email: string;
  phoneNumber: string;
  bio?: string | null;
  active: boolean;
  workingHoursStart?: string;
  workingHoursEnd?: string;
  workingDays?: string[];
}

/** @deprecated `Doctor` now carries the schedule fields directly (they're
 * real, backend-persisted columns) - kept as an alias so existing imports
 * don't need to change. */
export type DoctorWithSchedule = Doctor;
