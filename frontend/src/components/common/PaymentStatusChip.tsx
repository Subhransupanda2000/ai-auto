import { Chip } from '@mui/material';
import type { PaymentStatus } from '../../types/payment';

const statusColorMap: Record<PaymentStatus, 'default' | 'success' | 'warning' | 'error'> = {
  PENDING: 'warning',
  PAID: 'success',
  FAILED: 'error',
  CANCELLED: 'default',
};

const statusLabelMap: Record<PaymentStatus, string> = {
  PENDING: 'Pending',
  PAID: 'Paid',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
};

interface PaymentStatusChipProps {
  status: PaymentStatus;
  size?: 'small' | 'medium';
}

export function PaymentStatusChip({ status, size = 'small' }: PaymentStatusChipProps) {
  return <Chip label={statusLabelMap[status]} color={statusColorMap[status]} size={size} variant="filled" />;
}
