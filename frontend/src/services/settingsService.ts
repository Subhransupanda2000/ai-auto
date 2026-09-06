/**
 * The backend has no Settings API yet (clinic/reminder/AI config currently
 * lives only in `application.yml`). This service persists user-edited
 * settings to localStorage so the Settings page is fully functional; replace
 * with a real `api/settingsApi.ts` once the backend exposes one.
 */
import type { AppSettings } from '../types/settings';

const storageKey = 'hc_app_settings';

const defaultSettings: AppSettings = {
  clinic: {
    clinicName: 'Healthcare Clinic',
    timezone: 'UTC',
    address: '123 Main Street, Springfield',
    phoneNumber: '+1 (555) 010-2020',
    email: 'front-desk@healthcareclinic.example',
    appointmentSlotMinutes: 30,
    businessHours: [
      { day: 'MON', open: true, startTime: '09:00', endTime: '17:00' },
      { day: 'TUE', open: true, startTime: '09:00', endTime: '17:00' },
      { day: 'WED', open: true, startTime: '09:00', endTime: '17:00' },
      { day: 'THU', open: true, startTime: '09:00', endTime: '17:00' },
      { day: 'FRI', open: true, startTime: '09:00', endTime: '17:00' },
      { day: 'SAT', open: false, startTime: '09:00', endTime: '13:00' },
      { day: 'SUN', open: false, startTime: '09:00', endTime: '13:00' },
    ],
  },
  reminders: {
    remindersEnabled: true,
    reminderHoursBefore: 24,
    sendSmsReminders: false,
    sendEmailReminders: true,
    escalationEmail: '',
    escalationPhoneNumber: '',
  },
  ai: {
    model: 'gemini-flash-latest',
    temperature: 0.2,
    greetingMessage: "Hi! I'm your AI receptionist. How can I help you today?",
    autoBookingEnabled: true,
    escalateOnLowConfidence: true,
  },
  notifications: {
    newAppointmentAlerts: true,
    cancellationAlerts: true,
    dailySummaryEmail: false,
  },
};

export const settingsService = {
  get(): AppSettings {
    const raw = localStorage.getItem(storageKey);
    if (!raw) return defaultSettings;
    try {
      return { ...defaultSettings, ...(JSON.parse(raw) as AppSettings) };
    } catch {
      return defaultSettings;
    }
  },

  save(settings: AppSettings): void {
    localStorage.setItem(storageKey, JSON.stringify(settings));
  },

  reset(): AppSettings {
    localStorage.removeItem(storageKey);
    return defaultSettings;
  },
};
