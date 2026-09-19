import { useMemo, useState } from 'react';
import { useSnackbar } from 'notistack';
import {
  Avatar,
  Box,
  Button,
  Card,
  IconButton,
  InputAdornment,
  Menu,
  MenuItem,
  Stack,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
  TextField,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import EditCalendarRoundedIcon from '@mui/icons-material/EditCalendarRounded';
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import TodayRoundedIcon from '@mui/icons-material/TodayRounded';
import TaskAltRoundedIcon from '@mui/icons-material/TaskAltRounded';
import PaidRoundedIcon from '@mui/icons-material/PaidRounded';
import TableRowsRoundedIcon from '@mui/icons-material/TableRowsRounded';
import CalendarViewMonthRoundedIcon from '@mui/icons-material/CalendarViewMonthRounded';
import { format } from 'date-fns';
import { PageHero } from '../../components/common/PageHero';
import { StatCard } from '../../components/common/StatCard';
import { ConfirmDialog } from '../../components/common/ConfirmDialog';
import { StatusChip } from '../../components/common/StatusChip';
import { AppointmentFormDialog } from './AppointmentFormDialog';
import { AppointmentsCalendarView } from './AppointmentsCalendarView';
import {
  useAppointments,
  useCancelAppointment,
  useCreateAppointment,
  useUpdateAppointment,
} from '../../hooks/useAppointments';
import { useDoctors } from '../../hooks/useDoctors';
import { formatRupees } from '../../utils/currency';
import type { Appointment, AppointmentRequest } from '../../types/appointment';

export function AppointmentsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { data: appointments = [], isLoading } = useAppointments();
  const { data: doctors = [] } = useDoctors();
  const createAppointment = useCreateAppointment();
  const updateAppointment = useUpdateAppointment();
  const cancelAppointment = useCancelAppointment();

  const [view, setView] = useState<'table' | 'calendar'>('table');
  const [search, setSearch] = useState('');
  const [doctorFilter, setDoctorFilter] = useState('all');
  const [dateFilter, setDateFilter] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editingAppointment, setEditingAppointment] = useState<Appointment | null>(null);
  const [cancelTarget, setCancelTarget] = useState<Appointment | null>(null);
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const [menuAppointment, setMenuAppointment] = useState<Appointment | null>(null);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return appointments.filter((a) => {
      const matchesSearch = !q || `${a.patientName} ${a.doctorName}`.toLowerCase().includes(q);
      const matchesDoctor = doctorFilter === 'all' || a.doctorId === doctorFilter;
      const matchesDate = !dateFilter || format(new Date(a.scheduledStart), 'yyyy-MM-dd') === dateFilter;
      return matchesSearch && matchesDoctor && matchesDate;
    });
  }, [appointments, search, doctorFilter, dateFilter]);

  const stats = useMemo(() => {
    const todayStr = format(new Date(), 'yyyy-MM-dd');
    const today = appointments.filter((a) => format(new Date(a.scheduledStart), 'yyyy-MM-dd') === todayStr).length;
    const completed = appointments.filter((a) => a.status === 'COMPLETED');
    const upcoming = appointments.filter((a) => ['SCHEDULED', 'CONFIRMED', 'RESCHEDULED'].includes(a.status)).length;
    const revenue = completed.reduce((sum, a) => sum + (a.consultationFee ?? 0), 0);
    return { today, upcoming, completed: completed.length, revenue };
  }, [appointments]);

  const handleCreate = (values: AppointmentRequest) => {
    createAppointment.mutate(values, {
      onSuccess: () => {
        enqueueSnackbar('Appointment booked', { variant: 'success' });
        setFormOpen(false);
      },
      onError: (error) => enqueueSnackbar((error as { message?: string }).message ?? 'Failed to book appointment', { variant: 'error' }),
    });
  };

  const handleReschedule = (values: AppointmentRequest) => {
    if (!editingAppointment) return;
    updateAppointment.mutate(
      {
        id: editingAppointment.id,
        payload: {
          newStart: values.start,
          newEnd: values.end,
          reason: values.reason,
          consultationFee: values.consultationFee,
        },
      },
      {
        onSuccess: () => {
          enqueueSnackbar('Appointment rescheduled', { variant: 'success' });
          setFormOpen(false);
          setEditingAppointment(null);
        },
        onError: (error) => enqueueSnackbar((error as { message?: string }).message ?? 'Failed to reschedule', { variant: 'error' }),
      },
    );
  };

  const handleConfirm = (appointment: Appointment) => {
    updateAppointment.mutate(
      { id: appointment.id, payload: { status: 'CONFIRMED' } },
      { onSuccess: () => enqueueSnackbar('Appointment confirmed', { variant: 'success' }) },
    );
  };

  const handleComplete = (appointment: Appointment) => {
    updateAppointment.mutate(
      { id: appointment.id, payload: { status: 'COMPLETED' } },
      { onSuccess: () => enqueueSnackbar('Appointment marked completed', { variant: 'success' }) },
    );
  };

  const handleCancel = () => {
    if (!cancelTarget) return;
    cancelAppointment.mutate(
      { id: cancelTarget.id },
      {
        onSuccess: () => {
          enqueueSnackbar('Appointment cancelled', { variant: 'success' });
          setCancelTarget(null);
        },
      },
    );
  };

  const columns: GridColDef<Appointment>[] = [
    {
      field: 'patientName',
      headerName: 'Patient',
      flex: 1.2,
      minWidth: 180,
      renderCell: (params) => (
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ height: '100%' }}>
          <Avatar sx={{ width: 32, height: 32, fontSize: 13, bgcolor: 'primary.light' }}>
            {params.value?.charAt(0) ?? '?'}
          </Avatar>
          <Typography variant="body2" fontWeight={600}>
            {params.value}
          </Typography>
        </Stack>
      ),
    },
    { field: 'doctorName', headerName: 'Doctor', flex: 1, minWidth: 160 },
    {
      field: 'scheduledStart',
      headerName: 'Date & time',
      flex: 1,
      minWidth: 180,
      valueGetter: (value: string) => format(new Date(value), 'MMM d, yyyy h:mm a'),
    },
    { field: 'reason', headerName: 'Reason', flex: 1, minWidth: 160, valueGetter: (value) => value ?? '—' },
    {
      field: 'consultationFee',
      headerName: 'Fee',
      flex: 0.6,
      minWidth: 110,
      valueGetter: (value: number | null) => (value != null ? `₹${value.toLocaleString('en-IN')}` : '—'),
    },
    {
      field: 'status',
      headerName: 'Status',
      width: 140,
      renderCell: (params) => <StatusChip status={params.value} />,
    },
    {
      field: 'actions',
      headerName: '',
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      width: 60,
      renderCell: (params) => (
        <IconButton
          size="small"
          onClick={(e) => {
            setMenuAnchor(e.currentTarget);
            setMenuAppointment(params.row);
          }}
        >
          <MoreVertRoundedIcon fontSize="small" />
        </IconButton>
      ),
    },
  ];

  return (
    <Box>
      <PageHero
        title="Appointments"
        subtitle="Book, reschedule, and manage patient appointments."
        icon={<EventAvailableRoundedIcon />}
        gradient="linear-gradient(135deg, #2A50A8 0%, #3B6FE0 55%, #0FB5A7 130%)"
        actions={
          <Button
            variant="contained"
            startIcon={<AddRoundedIcon />}
            onClick={() => {
              setEditingAppointment(null);
              setFormOpen(true);
            }}
            sx={{
              bgcolor: '#FFFFFF',
              color: 'primary.dark',
              fontWeight: 700,
              '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' },
            }}
          >
            Book Appointment
          </Button>
        }
      />

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Today's Appointments" value={stats.today} icon={<TodayRoundedIcon />} color="primary" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Upcoming" value={stats.upcoming} icon={<EventAvailableRoundedIcon />} color="info" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Completed" value={stats.completed} icon={<TaskAltRoundedIcon />} color="success" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Revenue Collected" value={formatRupees(stats.revenue)} icon={<PaidRoundedIcon />} color="secondary" />
        </Grid>
      </Grid>

      <Card sx={{ p: 2, mb: 2.5 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              placeholder="Search by patient or doctor..."
              size="small"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              fullWidth
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchRoundedIcon fontSize="small" color="action" />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <TextField select size="small" label="Doctor" value={doctorFilter} onChange={(e) => setDoctorFilter(e.target.value)} fullWidth>
              <MenuItem value="all">All doctors</MenuItem>
              {doctors.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  Dr. {d.firstName} {d.lastName}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <TextField
              type="date"
              size="small"
              label="Date"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <ToggleButtonGroup
              value={view}
              exclusive
              size="small"
              onChange={(_e, v) => v && setView(v)}
              fullWidth
              sx={{
                bgcolor: 'action.hover',
                borderRadius: 2,
                p: 0.5,
                '& .MuiToggleButton-root': {
                  border: 'none',
                  borderRadius: 1.5,
                  fontWeight: 600,
                  '&.Mui-selected': { bgcolor: 'background.paper', boxShadow: '0 1px 3px rgba(15,23,42,0.12)' },
                },
              }}
            >
              <ToggleButton value="table">
                <TableRowsRoundedIcon fontSize="small" sx={{ mr: 0.75 }} /> Table
              </ToggleButton>
              <ToggleButton value="calendar">
                <CalendarViewMonthRoundedIcon fontSize="small" sx={{ mr: 0.75 }} /> Calendar
              </ToggleButton>
            </ToggleButtonGroup>
          </Grid>
        </Grid>
      </Card>

      {view === 'table' ? (
        <Card>
          <Box sx={{ height: 600 }}>
            <DataGrid
              rows={filtered}
              columns={columns}
              loading={isLoading}
              disableRowSelectionOnClick
              pageSizeOptions={[10, 25, 50]}
              initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
              rowHeight={64}
              sx={{
                border: 'none',
                '& .MuiDataGrid-columnHeaders': { bgcolor: 'action.hover' },
                '& .MuiDataGrid-cell': { display: 'flex', alignItems: 'center' },
                '& .MuiDataGrid-row:hover': { bgcolor: 'action.hover' },
              }}
            />
          </Box>
        </Card>
      ) : (
        <AppointmentsCalendarView
          appointments={filtered}
          onSelectAppointment={(appt) => {
            setEditingAppointment(appt);
            setFormOpen(true);
          }}
        />
      )}

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
        {menuAppointment?.status === 'SCHEDULED' && (
          <MenuItem
            onClick={() => {
              if (menuAppointment) handleConfirm(menuAppointment);
              setMenuAnchor(null);
            }}
          >
            Confirm
          </MenuItem>
        )}
        {(menuAppointment?.status === 'SCHEDULED' || menuAppointment?.status === 'CONFIRMED') && (
          <MenuItem
            onClick={() => {
              if (menuAppointment) handleComplete(menuAppointment);
              setMenuAnchor(null);
            }}
          >
            Mark completed
          </MenuItem>
        )}
        <MenuItem
          onClick={() => {
            setEditingAppointment(menuAppointment);
            setFormOpen(true);
            setMenuAnchor(null);
          }}
        >
          <EditCalendarRoundedIcon fontSize="small" sx={{ mr: 1.5 }} /> Reschedule
        </MenuItem>
        <MenuItem
          onClick={() => {
            setCancelTarget(menuAppointment);
            setMenuAnchor(null);
          }}
          sx={{ color: 'error.main' }}
        >
          <CancelRoundedIcon fontSize="small" sx={{ mr: 1.5 }} /> Cancel
        </MenuItem>
      </Menu>

      <AppointmentFormDialog
        open={formOpen}
        appointment={editingAppointment}
        loading={createAppointment.isPending || updateAppointment.isPending}
        onClose={() => {
          setFormOpen(false);
          setEditingAppointment(null);
        }}
        onSubmit={editingAppointment ? handleReschedule : handleCreate}
      />

      <ConfirmDialog
        open={Boolean(cancelTarget)}
        title="Cancel appointment"
        message={`Are you sure you want to cancel ${cancelTarget?.patientName}'s appointment with ${cancelTarget?.doctorName}?`}
        confirmLabel="Cancel appointment"
        destructive
        loading={cancelAppointment.isPending}
        onConfirm={handleCancel}
        onClose={() => setCancelTarget(null)}
      />
    </Box>
  );
}
