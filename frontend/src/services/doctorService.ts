import { doctorsApi } from '../api/doctorsApi';
import { LocalOverlayStore } from './localOverlayStore';
import type { Doctor, DoctorRequest, DoctorWithSchedule } from '../types/doctor';

const overlay = new LocalOverlayStore<Doctor>('hc_doctors_overlay');
const scheduleKey = 'hc_doctor_schedules';

const DEFAULT_SCHEDULE = {
  workingHoursStart: '09:00',
  workingHoursEnd: '17:00',
  workingDays: ['MON', 'TUE', 'WED', 'THU', 'FRI'],
};

function readSchedules(): Record<string, Pick<DoctorWithSchedule, 'workingHoursStart' | 'workingHoursEnd' | 'workingDays'>> {
  const raw = localStorage.getItem(scheduleKey);
  if (!raw) return {};
  try {
    return JSON.parse(raw);
  } catch {
    return {};
  }
}

function writeSchedules(data: ReturnType<typeof readSchedules>): void {
  localStorage.setItem(scheduleKey, JSON.stringify(data));
}

function withSchedule(doctor: Doctor): DoctorWithSchedule {
  const schedules = readSchedules();
  return { ...doctor, ...(schedules[doctor.id] ?? DEFAULT_SCHEDULE) };
}

export const doctorService = {
  async list(specialty?: string): Promise<DoctorWithSchedule[]> {
    const server = await doctorsApi.list(specialty);
    return overlay.merge(server).map(withSchedule);
  },

  async get(id: string): Promise<DoctorWithSchedule | undefined> {
    const all = await doctorService.list();
    return all.find((d) => d.id === id);
  },

  create(payload: DoctorRequest): DoctorWithSchedule {
    const created = overlay.create({
      firstName: payload.firstName,
      lastName: payload.lastName,
      specialty: payload.specialty,
      email: payload.email,
      phoneNumber: payload.phoneNumber,
      bio: payload.bio ?? null,
      active: payload.active,
    });
    doctorService.updateSchedule(created.id, {
      workingHoursStart: payload.workingHoursStart ?? DEFAULT_SCHEDULE.workingHoursStart,
      workingHoursEnd: payload.workingHoursEnd ?? DEFAULT_SCHEDULE.workingHoursEnd,
      workingDays: payload.workingDays ?? DEFAULT_SCHEDULE.workingDays,
    });
    return withSchedule(created);
  },

  update(id: string, payload: Partial<DoctorRequest>): void {
    const { workingHoursStart, workingHoursEnd, workingDays, ...doctorFields } = payload;
    overlay.update(id, doctorFields);
    if (workingHoursStart || workingHoursEnd || workingDays) {
      doctorService.updateSchedule(id, {
        workingHoursStart: workingHoursStart ?? DEFAULT_SCHEDULE.workingHoursStart,
        workingHoursEnd: workingHoursEnd ?? DEFAULT_SCHEDULE.workingHoursEnd,
        workingDays: workingDays ?? DEFAULT_SCHEDULE.workingDays,
      });
    }
  },

  updateSchedule(
    id: string,
    schedule: Pick<DoctorWithSchedule, 'workingHoursStart' | 'workingHoursEnd' | 'workingDays'>,
  ): void {
    const schedules = readSchedules();
    schedules[id] = schedule;
    writeSchedules(schedules);
  },

  remove(id: string): void {
    overlay.remove(id);
  },
};
