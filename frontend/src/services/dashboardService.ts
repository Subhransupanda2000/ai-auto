/**
 * The backend has no dedicated analytics endpoint (aside from
 * GET /api/appointments/revenue, used for the dashboard's real,
 * date-range-filtered revenue figure), so everything else here is a pure
 * client-side aggregation computed from the real Patients/Doctors/
 * Appointments data.
 */
import { isSameDay, isToday, startOfDay, subDays, format } from 'date-fns';
import type { Appointment } from '../types/appointment';
import type { Doctor } from '../types/doctor';
import type { Patient } from '../types/patient';

export interface DashboardStats {
  totalPatients: number;
  todaysAppointments: number;
  totalDoctors: number;
  activeDoctors: number;
  upcomingAppointments: Appointment[];
}

export interface DailyPoint {
  date: string;
  label: string;
  value: number;
}

export function computeStats(
  patients: Patient[],
  doctors: Doctor[],
  appointments: Appointment[],
): DashboardStats {
  const todaysAppointments = appointments.filter((a) => isToday(new Date(a.scheduledStart))).length;
  const upcomingAppointments = appointments
    .filter((a) => new Date(a.scheduledStart).getTime() >= Date.now() && a.status !== 'CANCELLED')
    .sort((a, b) => a.scheduledStart.localeCompare(b.scheduledStart))
    .slice(0, 6);

  return {
    totalPatients: patients.length,
    todaysAppointments,
    totalDoctors: doctors.length,
    activeDoctors: doctors.filter((d) => d.active).length,
    upcomingAppointments,
  };
}

export function appointmentsPerDay(appointments: Appointment[], days = 14): DailyPoint[] {
  const points: DailyPoint[] = [];
  for (let i = days - 1; i >= 0; i -= 1) {
    const day = startOfDay(subDays(new Date(), i));
    const count = appointments.filter((a) => isSameDay(new Date(a.scheduledStart), day)).length;
    points.push({ date: day.toISOString(), label: format(day, 'MMM d'), value: count });
  }
  return points;
}

/** Real revenue per day (sum of consultationFee on completed appointments),
 * not the flat per-appointment estimate - consultationFee is entered by
 * staff at booking time (see AppointmentFormDialog). */
export function revenueTrend(appointments: Appointment[], days = 14): DailyPoint[] {
  const points: DailyPoint[] = [];
  for (let i = days - 1; i >= 0; i -= 1) {
    const day = startOfDay(subDays(new Date(), i));
    const revenue = appointments
      .filter((a) => isSameDay(new Date(a.scheduledStart), day) && a.status === 'COMPLETED')
      .reduce((sum, a) => sum + (a.consultationFee ?? 0), 0);
    points.push({
      date: day.toISOString(),
      label: format(day, 'MMM d'),
      value: revenue,
    });
  }
  return points;
}

export interface DashboardTrends {
  appointmentsTrend: number;
  revenueTrend: number;
  newPatientsTrend: number;
}

function percentChange(current: number, previous: number): number {
  if (previous === 0) return current > 0 ? 100 : 0;
  return Math.round(((current - previous) / previous) * 100);
}

/** Week-over-week % change for appointment volume, estimated revenue, and new
 * patients (using first-appointment date as a proxy, same as patientGrowthTrend). */
export function computeTrends(appointments: Appointment[]): DashboardTrends {
  const last7Start = startOfDay(subDays(new Date(), 6)).getTime();
  const prev7Start = startOfDay(subDays(new Date(), 13)).getTime();

  const last7 = appointments.filter((a) => new Date(a.scheduledStart).getTime() >= last7Start);
  const prev7 = appointments.filter((a) => {
    const t = new Date(a.scheduledStart).getTime();
    return t >= prev7Start && t < last7Start;
  });

  const revenueOf = (list: Appointment[]) =>
    list
      .filter((a) => a.status === 'COMPLETED')
      .reduce((sum, a) => sum + (a.consultationFee ?? 0), 0);

  const firstSeenByPatient = new Map<string, number>();
  for (const appointment of appointments) {
    const time = new Date(appointment.scheduledStart).getTime();
    const existing = firstSeenByPatient.get(appointment.patientId);
    if (existing === undefined || time < existing) {
      firstSeenByPatient.set(appointment.patientId, time);
    }
  }
  const newPatientsSince = (since: number, until?: number) =>
    [...firstSeenByPatient.values()].filter((t) => t >= since && (until === undefined || t < until)).length;

  return {
    appointmentsTrend: percentChange(last7.length, prev7.length),
    revenueTrend: percentChange(revenueOf(last7), revenueOf(prev7)),
    newPatientsTrend: percentChange(
      newPatientsSince(last7Start),
      newPatientsSince(prev7Start, last7Start),
    ),
  };
}

/** Cumulative patient count over time. Patients have no createdAt field in
 * the backend response, so this approximates growth using booking history
 * (first appointment date) as a proxy, falling back to an even distribution. */
export function patientGrowthTrend(patients: Patient[], appointments: Appointment[], days = 14): DailyPoint[] {
  const firstSeenByPatient = new Map<string, number>();
  for (const appointment of appointments) {
    const time = new Date(appointment.scheduledStart).getTime();
    const existing = firstSeenByPatient.get(appointment.patientId);
    if (existing === undefined || time < existing) {
      firstSeenByPatient.set(appointment.patientId, time);
    }
  }

  const points: DailyPoint[] = [];
  const windowStart = startOfDay(subDays(new Date(), days - 1)).getTime();
  let baseline = patients.length - firstSeenByPatient.size;
  if (baseline < 0) baseline = 0;

  let cumulative = baseline;
  for (let i = days - 1; i >= 0; i -= 1) {
    const day = startOfDay(subDays(new Date(), i));
    const dayEnd = day.getTime() + 24 * 60 * 60 * 1000;
    const newToday = [...firstSeenByPatient.values()].filter(
      (t) => t >= day.getTime() && t < dayEnd && day.getTime() >= windowStart,
    ).length;
    cumulative += newToday;
    points.push({ date: day.toISOString(), label: format(day, 'MMM d'), value: cumulative });
  }
  return points;
}
