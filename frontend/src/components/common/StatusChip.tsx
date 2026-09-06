import { Chip } from '@mui/material';
import type { AppointmentStatus } from '../../types/appointment';

const statusColorMap: Record<AppointmentStatus, 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'> = {
  SCHEDULED: 'info',
  CONFIRMED: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'error',
  RESCHEDULED: 'warning',
  NO_SHOW: 'default',
};

const statusLabelMap: Record<AppointmentStatus, string> = {
  SCHEDULED: 'Scheduled',
  CONFIRMED: 'Confirmed',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  RESCHEDULED: 'Rescheduled',
  NO_SHOW: 'No Show',
};

interface StatusChipProps {
  status: AppointmentStatus;
  size?: 'small' | 'medium';
}

export function StatusChip({ status, size = 'small' }: StatusChipProps) {
  return <Chip label={statusLabelMap[status]} color={statusColorMap[status]} size={size} variant="filled" />;
}
