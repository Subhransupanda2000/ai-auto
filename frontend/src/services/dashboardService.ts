/**
 * The backend has no analytics/dashboard endpoint, so these are pure
 * client-side aggregations computed from the real Patients/Doctors/
 * Appointments data. Revenue is not modeled server-side at all (no billing
 * entity), so it is derived with a fixed per-completed-appointment estimate
 * purely for visualization purposes and clearly labeled as an estimate in
 * the UI.
 */
import { isSameDay, isToday, startOfDay, subDays, format } from 'date-fns';
import type { Appointment } from '../types/appointment';
import type { Doctor } from '../types/doctor';
import type { Patient } from '../types/patient';

const ESTIMATED_REVENUE_PER_COMPLETED_APPOINTMENT = 120;

export interface DashboardStats {
  totalPatients: number;
  todaysAppointments: number;
  totalDoctors: number;
  activeDoctors: number;
  estimatedRevenue: number;
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
  const completedCount = appointments.filter((a) => a.status === 'COMPLETED').length;
  const upcomingAppointments = appointments
    .filter((a) => new Date(a.scheduledStart).getTime() >= Date.now() && a.status !== 'CANCELLED')
    .sort((a, b) => a.scheduledStart.localeCompare(b.scheduledStart))
    .slice(0, 6);

  return {
    totalPatients: patients.length,
    todaysAppointments,
    totalDoctors: doctors.length,
    activeDoctors: doctors.filter((d) => d.active).length,
    estimatedRevenue: completedCount * ESTIMATED_REVENUE_PER_COMPLETED_APPOINTMENT,
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

export function revenueTrend(appointments: Appointment[], days = 14): DailyPoint[] {
  const points: DailyPoint[] = [];
  for (let i = days - 1; i >= 0; i -= 1) {
    const day = startOfDay(subDays(new Date(), i));
    const completed = appointments.filter(
      (a) => isSameDay(new Date(a.scheduledStart), day) && a.status === 'COMPLETED',
    ).length;
    points.push({
      date: day.toISOString(),
      label: format(day, 'MMM d'),
      value: completed * ESTIMATED_REVENUE_PER_COMPLETED_APPOINTMENT,
    });
  }
  return points;
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
