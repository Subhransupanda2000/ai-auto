import { patientsApi } from '../api/patientsApi';
import type { Patient, PatientMedicalNote, PatientRequest } from '../types/patient';
import { generateId } from '../utils/id';

const notesKey = 'hc_patient_notes';

function readNotes(): PatientMedicalNote[] {
  const raw = localStorage.getItem(notesKey);
  if (!raw) return [];
  try {
    return JSON.parse(raw) as PatientMedicalNote[];
  } catch {
    return [];
  }
}

function writeNotes(notes: PatientMedicalNote[]): void {
  localStorage.setItem(notesKey, JSON.stringify(notes));
}

export const patientService = {
  async list(): Promise<Patient[]> {
    return patientsApi.list();
  },

  async get(id: string): Promise<Patient | undefined> {
    return patientsApi.get(id);
  },

  create(payload: PatientRequest): Promise<Patient> {
    return patientsApi.create(payload);
  },

  update(id: string, payload: Partial<PatientRequest>): Promise<Patient> {
    return patientsApi.update(id, payload);
  },

  listNotes(patientId: string): PatientMedicalNote[] {
    return readNotes()
      .filter((n) => n.patientId === patientId)
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  },

  addNote(patientId: string, note: string, createdBy: string): PatientMedicalNote {
    const record: PatientMedicalNote = {
      id: generateId(),
      patientId,
      note,
      createdAt: new Date().toISOString(),
      createdBy,
    };
    writeNotes([record, ...readNotes()]);
    return record;
  },

  removeNote(id: string): void {
    writeNotes(readNotes().filter((n) => n.id !== id));
  },
};
