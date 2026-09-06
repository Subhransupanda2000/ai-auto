export type AppointmentStatus =
  | 'SCHEDULED'
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'COMPLETED'
  | 'RESCHEDULED'
  | 'NO_SHOW';

export interface Appointment {
  id: string;
  patientId: string;
  patientName: string;
  doctorId: string;
  doctorName: string;
  scheduledStart: string;
  scheduledEnd: string;
  status: AppointmentStatus;
  reason: string | null;
  notes: string | null;
}

export interface AppointmentRequest {
  patientId: string;
  doctorId: string;
  start: string;
  end: string;
  reason?: string;
}

export interface AppointmentUpdateRequest {
  newStart?: string;
  newEnd?: string;
  reason?: string;
  status?: AppointmentStatus;
}

export interface AppointmentCancelRequest {
  reason?: string;
}

export interface AvailableSlot {
  start: string;
  end: string;
}
