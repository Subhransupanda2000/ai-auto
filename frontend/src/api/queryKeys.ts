export const queryKeys = {
  patients: ['patients'] as const,
  patient: (id: string) => ['patients', id] as const,
  patientNotes: (id: string) => ['patients', id, 'notes'] as const,
  doctors: ['doctors'] as const,
  doctor: (id: string) => ['doctors', id] as const,
  appointments: (filters?: { patientId?: string; doctorId?: string }) =>
    ['appointments', filters ?? {}] as const,
  knowledgeBase: ['knowledge-base'] as const,
  settings: ['settings'] as const,
  chatConversations: ['chat', 'conversations'] as const,
};
