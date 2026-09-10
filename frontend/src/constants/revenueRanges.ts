import type { RevenueRange } from '../types/appointment';

export const REVENUE_RANGE_OPTIONS: { value: RevenueRange; label: string }[] = [
  { value: 'TODAY', label: 'Today' },
  { value: 'YESTERDAY', label: 'Yesterday' },
  { value: 'LAST_7_DAYS', label: 'Last 7 Days' },
  { value: 'LAST_MONTH', label: 'Last Month' },
  { value: 'LAST_6_MONTHS', label: 'Last 6 Months' },
  { value: 'LAST_YEAR', label: 'Last Year' },
];
