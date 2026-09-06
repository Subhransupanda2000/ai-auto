export interface BusinessHours {
  day: 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' | 'SUN';
  open: boolean;
  startTime: string;
  endTime: string;
}

export interface ClinicSettings {
  clinicName: string;
  timezone: string;
  address: string;
  phoneNumber: string;
  email: string;
  appointmentSlotMinutes: number;
  businessHours: BusinessHours[];
}

export interface ReminderSettings {
  remindersEnabled: boolean;
  reminderHoursBefore: number;
  sendSmsReminders: boolean;
  sendEmailReminders: boolean;
  escalationEmail: string;
  escalationPhoneNumber: string;
}

export interface AiSettings {
  model: string;
  temperature: number;
  greetingMessage: string;
  autoBookingEnabled: boolean;
  escalateOnLowConfidence: boolean;
}

export interface NotificationSettings {
  newAppointmentAlerts: boolean;
  cancellationAlerts: boolean;
  dailySummaryEmail: boolean;
}

export interface AppSettings {
  clinic: ClinicSettings;
  reminders: ReminderSettings;
  ai: AiSettings;
  notifications: NotificationSettings;
}
