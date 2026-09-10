import { apiClient } from './client';
import type { Doctor, DoctorRequest } from '../types/doctor';

export const doctorsApi = {
  list: (specialty?: string) =>
    apiClient
      .get<Doctor[]>('/doctors', { params: specialty ? { specialty } : undefined })
      .then((res) => res.data),

  get: (id: string) => apiClient.get<Doctor>(`/doctors/${id}`).then((res) => res.data),

  create: (payload: DoctorRequest) =>
    apiClient.post<Doctor>('/doctors', payload).then((res) => res.data),

  update: (id: string, payload: Partial<DoctorRequest>) =>
    apiClient.put<Doctor>(`/doctors/${id}`, payload).then((res) => res.data),
};
