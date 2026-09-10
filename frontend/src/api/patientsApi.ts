import { apiClient } from './client';
import type { Patient, PatientRequest } from '../types/patient';

export const patientsApi = {
  list: () => apiClient.get<Patient[]>('/patients').then((res) => res.data),

  get: (id: string) => apiClient.get<Patient>(`/patients/${id}`).then((res) => res.data),

  create: (payload: PatientRequest) =>
    apiClient.post<Patient>('/patients', payload).then((res) => res.data),

  update: (id: string, payload: Partial<PatientRequest>) =>
    apiClient.put<Patient>(`/patients/${id}`, payload).then((res) => res.data),
};
