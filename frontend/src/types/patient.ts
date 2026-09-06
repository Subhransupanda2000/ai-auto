export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'UNSPECIFIED';

export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email: string | null;
  dateOfBirth: string | null;
  gender: string | null;
}

export interface PatientMedicalNote {
  id: string;
  patientId: string;
  note: string;
  createdAt: string;
  createdBy: string;
}

export interface PatientRequest {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email?: string | null;
  dateOfBirth?: string | null;
  gender?: string | null;
}
