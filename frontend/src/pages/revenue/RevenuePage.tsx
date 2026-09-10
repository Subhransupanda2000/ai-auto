import { useMemo, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { addDays, format, parseISO, startOfDay, subDays, subMonths, subYears } from 'date-fns';
import PaidRoundedIcon from '@mui/icons-material/PaidRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import ReceiptLongRoundedIcon from '@mui/icons-material/ReceiptLongRounded';
import { PageHeader } from '../../components/common/PageHeader';
import { LoadingState } from '../../components/common/LoadingState';
import { EmptyState } from '../../components/common/EmptyState';
import { useAppointments } from '../../hooks/useAppointments';
import { REVENUE_RANGE_OPTIONS } from '../../constants/revenueRanges';
import { formatRupees } from '../../utils/currency';
import type { Appointment, RevenueRange } from '../../types/appointment';

type FilterOption = RevenueRange | 'CUSTOM';

/** Rolling window matching the backend's AppointmentServiceImpl.getRevenueSummary
 * semantics, so the quick-preset numbers here always agree with the dashboard. */
function resolvePresetWindow(preset: RevenueRange): { from: Date; to: Date } {
  const today = startOfDay(new Date());
  const to = addDays(today, 1);
  switch (preset) {
    case 'TODAY':
      return { from: today, to };
    case 'YESTERDAY':
      return { from: subDays(today, 1), to: today };
    case 'LAST_7_DAYS':
      return { from: subDays(today, 6), to };
    case 'LAST_MONTH':
      return { from: addDays(subMonths(today, 1), 1), to };
    case 'LAST_6_MONTHS':
      return { from: addDays(subMonths(today, 6), 1), to };
    case 'LAST_YEAR':
      return { from: addDays(subYears(today, 1), 1), to };
    default:
      return { from: today, to };
  }
}

export function RevenuePage() {
  const { data: appointments = [], isLoading } = useAppointments();
  const [preset, setPreset] = useState<FilterOption>('TODAY');
  const [customFrom, setCustomFrom] = useState('');
  const [customTo, setCustomTo] = useState('');

  const { from, to } = useMemo(() => {
    if (preset === 'CUSTOM') {
      return {
        from: customFrom ? startOfDay(parseISO(customFrom)) : null,
        to: customTo ? addDays(startOfDay(parseISO(customTo)), 1) : null,
      };
    }
    return resolvePresetWindow(preset);
  }, [preset, customFrom, customTo]);

  const completedInRange = useMemo(() => {
    return appointments
      .filter((a) => a.status === 'COMPLETED')
      .filter((a) => {
        const start = new Date(a.scheduledStart);
        if (from && start < from) return false;
        if (to && start >= to) return false;
        return true;
      })
      .sort((a, b) => b.scheduledStart.localeCompare(a.scheduledStart));
  }, [appointments, from, to]);

  const totalRevenue = completedInRange.reduce((sum, a) => sum + (a.consultationFee ?? 0), 0);
  const visitCount = completedInRange.length;
  const averagePerVisit = visitCount > 0 ? totalRevenue / visitCount : 0;

  const columns: GridColDef<Appointment>[] = [
    {
      field: 'scheduledStart',
      headerName: 'Date & time',
      flex: 1,
      minWidth: 180,
      valueGetter: (value: string) => format(new Date(value), 'MMM d, yyyy h:mm a'),
    },
    { field: 'patientName', headerName: 'Patient', flex: 1, minWidth: 160 },
    { field: 'doctorName', headerName: 'Doctor', flex: 1, minWidth: 160 },
    { field: 'reason', headerName: 'Reason', flex: 1, minWidth: 160, valueGetter: (value) => value ?? '—' },
    {
      field: 'consultationFee',
      headerName: 'Fee',
      flex: 0.6,
      minWidth: 110,
      valueGetter: (value: number | null) => (value != null ? formatRupees(value) : '—'),
    },
  ];

  if (isLoading) {
    return <LoadingState label="Loading revenue..." minHeight={400} />;
  }

  return (
    <Box>
      <PageHeader
        title="Revenue"
        subtitle="Completed-visit revenue history, filterable by date."
      />

      <Card sx={{ mb: 2.5 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
            <Select
              size="small"
              value={preset}
              onChange={(e) => setPreset(e.target.value as FilterOption)}
              sx={{ minWidth: 180 }}
            >
              {REVENUE_RANGE_OPTIONS.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
              <MenuItem value="CUSTOM">Custom range</MenuItem>
            </Select>

            {preset === 'CUSTOM' && (
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <TextField
                  type="date"
                  label="From"
                  size="small"
                  value={customFrom}
                  onChange={(e) => setCustomFrom(e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                />
                <TextField
                  type="date"
                  label="To"
                  size="small"
                  value={customTo}
                  onChange={(e) => setCustomTo(e.target.value)}
                  slotProps={{ inputLabel: { shrink: true } }}
                />
              </Stack>
            )}
          </Stack>
        </CardContent>
      </Card>

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1.5}>
                <Box
                  sx={{
                    width: 46,
                    height: 46,
                    borderRadius: 2.5,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    bgcolor: (theme) => `${theme.palette.success.main}1F`,
                    color: 'success.main',
                  }}
                >
                  <PaidRoundedIcon />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary" fontWeight={600}>
                    Total revenue
                  </Typography>
                  <Typography variant="h5">{formatRupees(totalRevenue)}</Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1.5}>
                <Box
                  sx={{
                    width: 46,
                    height: 46,
                    borderRadius: 2.5,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    bgcolor: (theme) => `${theme.palette.primary.main}1F`,
                    color: 'primary.main',
                  }}
                >
                  <EventAvailableRoundedIcon />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary" fontWeight={600}>
                    Completed visits
                  </Typography>
                  <Typography variant="h5">{visitCount}</Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="center" spacing={1.5}>
                <Box
                  sx={{
                    width: 46,
                    height: 46,
                    borderRadius: 2.5,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    bgcolor: (theme) => `${theme.palette.secondary.main}1F`,
                    color: 'secondary.main',
                  }}
                >
                  <ReceiptLongRoundedIcon />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary" fontWeight={600}>
                    Average per visit
                  </Typography>
                  <Typography variant="h5">{formatRupees(averagePerVisit)}</Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        {completedInRange.length === 0 ? (
          <CardContent>
            <EmptyState title="No completed appointments in this range" />
          </CardContent>
        ) : (
          <Box sx={{ height: 600 }}>
            <DataGrid
              rows={completedInRange}
              columns={columns}
              disableRowSelectionOnClick
              pageSizeOptions={[10, 25, 50]}
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
              sx={{ border: 'none' }}
            />
          </Box>
        )}
      </Card>
    </Box>
  );
}
