import { apiClient } from './client';
import type { Patient } from '../types/patient';

/**
 * The backend currently only exposes read endpoints for patients
 * (GET /api/patients, GET /api/patients/{id}). Create/update/delete are not
 * yet implemented server-side; see `services/patientLocalService.ts` for the
 * client-side fallback used by the Patients page until those endpoints ship.
 */
export const patientsApi = {
  list: () => apiClient.get<Patient[]>('/patients').then((res) => res.data),

  get: (id: string) => apiClient.get<Patient>(`/patients/${id}`).then((res) => res.data),
};
