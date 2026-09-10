import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../api/queryKeys';
import { patientService } from '../services/patientService';
import type { PatientRequest } from '../types/patient';

export function usePatients() {
  return useQuery({
    queryKey: queryKeys.patients,
    queryFn: patientService.list,
  });
}

export function usePatient(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.patient(id ?? ''),
    queryFn: () => patientService.get(id as string),
    enabled: Boolean(id),
  });
}

export function usePatientNotes(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.patientNotes(id ?? ''),
    queryFn: () => patientService.listNotes(id as string),
    enabled: Boolean(id),
  });
}

export function useCreatePatient() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: PatientRequest) => patientService.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.patients }),
  });
}

export function useUpdatePatient() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: string; payload: Partial<PatientRequest> }) =>
      patientService.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.patients }),
  });
}

export function useAddPatientNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ patientId, note, createdBy }: { patientId: string; note: string; createdBy: string }) =>
      patientService.addNote(patientId, note, createdBy),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: queryKeys.patientNotes(variables.patientId) }),
  });
}
