import { apiClient } from './client';
import type { Doctor } from '../types/doctor';

/**
 * The backend currently only exposes read endpoints for doctors
 * (GET /api/doctors, GET /api/doctors/{id}). Create/update/delete and
 * schedule management are not yet implemented server-side; see
 * `services/doctorLocalService.ts` for the client-side fallback used by the
 * Doctors page until those endpoints ship.
 */
export const doctorsApi = {
  list: (specialty?: string) =>
    apiClient
      .get<Doctor[]>('/doctors', { params: specialty ? { specialty } : undefined })
      .then((res) => res.data),

  get: (id: string) => apiClient.get<Doctor>(`/doctors/${id}`).then((res) => res.data),
};
