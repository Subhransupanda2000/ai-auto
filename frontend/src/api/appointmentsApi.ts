import { apiClient } from './client';
import type {
  Appointment,
  AppointmentCancelRequest,
  AppointmentRequest,
  AppointmentUpdateRequest,
  RevenueRange,
  RevenueResponse,
} from '../types/appointment';

export const appointmentsApi = {
  list: (filters?: { patientId?: string; doctorId?: string }) =>
    apiClient
      .get<Appointment[]>('/appointments', { params: filters })
      .then((res) => res.data),

  create: (payload: AppointmentRequest) =>
    apiClient.post<Appointment>('/appointments', payload).then((res) => res.data),

  update: (id: string, payload: AppointmentUpdateRequest) =>
    apiClient.put<Appointment>(`/appointments/${id}`, payload).then((res) => res.data),

  cancel: (id: string, payload?: AppointmentCancelRequest) =>
    apiClient.delete<Appointment>(`/appointments/${id}`, { data: payload }).then((res) => res.data),

  revenue: (range: RevenueRange) =>
    apiClient.get<RevenueResponse>('/appointments/revenue', { params: { range } }).then((res) => res.data),
};
