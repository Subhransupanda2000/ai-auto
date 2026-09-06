import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { appointmentsApi } from '../api/appointmentsApi';
import { queryKeys } from '../api/queryKeys';
import type {
  AppointmentCancelRequest,
  AppointmentRequest,
  AppointmentUpdateRequest,
} from '../types/appointment';

export function useAppointments(filters?: { patientId?: string; doctorId?: string }) {
  return useQuery({
    queryKey: queryKeys.appointments(filters),
    queryFn: () => appointmentsApi.list(filters),
  });
}

export function useCreateAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: AppointmentRequest) => appointmentsApi.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  });
}

export function useUpdateAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AppointmentUpdateRequest }) =>
      appointmentsApi.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  });
}

export function useCancelAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload?: AppointmentCancelRequest }) =>
      appointmentsApi.cancel(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  });
}
