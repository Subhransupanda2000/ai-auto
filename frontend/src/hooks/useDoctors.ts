import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../api/queryKeys';
import { doctorService } from '../services/doctorService';
import type { DoctorRequest } from '../types/doctor';

export function useDoctors(specialty?: string) {
  return useQuery({
    queryKey: [...queryKeys.doctors, specialty ?? null],
    queryFn: () => doctorService.list(specialty),
  });
}

export function useDoctor(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.doctor(id ?? ''),
    queryFn: () => doctorService.get(id as string),
    enabled: Boolean(id),
  });
}

export function useCreateDoctor() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: DoctorRequest) => doctorService.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.doctors }),
  });
}

export function useUpdateDoctor() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: string; payload: Partial<DoctorRequest> }) =>
      doctorService.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.doctors }),
  });
}

export function useDeleteDoctor() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => doctorService.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.doctors }),
  });
}
