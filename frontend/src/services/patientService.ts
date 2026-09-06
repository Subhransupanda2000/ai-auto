import { patientsApi } from '../api/patientsApi';
import { LocalOverlayStore } from './localOverlayStore';
import type { Patient, PatientMedicalNote, PatientRequest } from '../types/patient';
import { generateId } from '../utils/id';

const overlay = new LocalOverlayStore<Patient>('hc_patients_overlay');
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
    const server = await patientsApi.list();
    return overlay.merge(server);
  },

  async get(id: string): Promise<Patient | undefined> {
    const all = await patientService.list();
    return all.find((p) => p.id === id);
  },

  create(payload: PatientRequest): Patient {
    return overlay.create({
      firstName: payload.firstName,
      lastName: payload.lastName,
      phoneNumber: payload.phoneNumber,
      email: payload.email ?? null,
      dateOfBirth: payload.dateOfBirth ?? null,
      gender: payload.gender ?? null,
    });
  },

  update(id: string, payload: Partial<PatientRequest>): void {
    overlay.update(id, payload);
  },

  remove(id: string): void {
    overlay.remove(id);
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
