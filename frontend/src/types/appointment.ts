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
  consultationFee: number | null;
}

export interface AppointmentRequest {
  patientId: string;
  doctorId: string;
  start: string;
  end: string;
  reason?: string;
  consultationFee?: number | null;
}

export interface AppointmentUpdateRequest {
  newStart?: string;
  newEnd?: string;
  reason?: string;
  status?: AppointmentStatus;
  consultationFee?: number | null;
}

export interface AppointmentCancelRequest {
  reason?: string;
}

export interface AvailableSlot {
  start: string;
  end: string;
}

export type RevenueRange = 'TODAY' | 'YESTERDAY' | 'LAST_7_DAYS' | 'LAST_MONTH' | 'LAST_6_MONTHS' | 'LAST_YEAR';

export interface RevenueResponse {
  range: RevenueRange;
  totalRevenue: number;
  completedAppointments: number;
  rangeStart: string;
  rangeEnd: string;
}
