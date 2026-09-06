import { usePatients } from './usePatients';
import { useDoctors } from './useDoctors';
import { useAppointments } from './useAppointments';
import {
  appointmentsPerDay,
  computeStats,
  patientGrowthTrend,
  revenueTrend,
} from '../services/dashboardService';

export function useDashboardStats() {
  const patientsQuery = usePatients();
  const doctorsQuery = useDoctors();
  const appointmentsQuery = useAppointments();

  const patients = patientsQuery.data ?? [];
  const doctors = doctorsQuery.data ?? [];
  const appointments = appointmentsQuery.data ?? [];

  const isLoading = patientsQuery.isLoading || doctorsQuery.isLoading || appointmentsQuery.isLoading;
  const isError = patientsQuery.isError || doctorsQuery.isError || appointmentsQuery.isError;

  return {
    isLoading,
    isError,
    stats: computeStats(patients, doctors, appointments),
    appointmentsPerDay: appointmentsPerDay(appointments),
    revenueTrend: revenueTrend(appointments),
    patientGrowth: patientGrowthTrend(patients, appointments),
    doctors,
    patients,
    appointments,
  };
}
