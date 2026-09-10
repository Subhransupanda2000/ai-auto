import { doctorsApi } from '../api/doctorsApi';
import type { Doctor, DoctorRequest } from '../types/doctor';

export const doctorService = {
  async list(specialty?: string): Promise<Doctor[]> {
    return doctorsApi.list(specialty);
  },

  async get(id: string): Promise<Doctor | undefined> {
    return doctorsApi.get(id);
  },

  create(payload: DoctorRequest): Promise<Doctor> {
    return doctorsApi.create(payload);
  },

  update(id: string, payload: Partial<DoctorRequest>): Promise<Doctor> {
    return doctorsApi.update(id, payload);
  },

  /** Doctors have no hard-delete endpoint (removing one would orphan/cascade
   * their appointment history); "delete" from the UI instead deactivates
   * them, which is what active/inactive already means everywhere else in
   * the app. */
  remove(id: string): Promise<Doctor> {
    return doctorsApi.update(id, { active: false });
  },
};
