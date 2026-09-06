export interface Doctor {
  id: string;
  firstName: string;
  lastName: string;
  specialty: string;
  email: string;
  phoneNumber: string;
  bio: string | null;
  active: boolean;
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

/** Extends the backend DoctorResponse with schedule fields that are not yet
 * modeled server-side; managed client-side until the backend adds support. */
export interface DoctorWithSchedule extends Doctor {
  workingHoursStart: string;
  workingHoursEnd: string;
  workingDays: string[];
}
