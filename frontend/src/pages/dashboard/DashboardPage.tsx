import { useState } from 'react';
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Chip,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  MenuItem,
  Select,
  Stack,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { useNavigate } from 'react-router-dom';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import MedicalServicesRoundedIcon from '@mui/icons-material/MedicalServicesRounded';
import PaidRoundedIcon from '@mui/icons-material/PaidRounded';
import PersonAddRoundedIcon from '@mui/icons-material/PersonAddRounded';
import CalendarMonthRoundedIcon from '@mui/icons-material/CalendarMonthRounded';
import SmartToyRoundedIcon from '@mui/icons-material/SmartToyRounded';
import InsightsRoundedIcon from '@mui/icons-material/InsightsRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/StatCard';
import { StatusChip } from '../../components/common/StatusChip';
import { LoadingState } from '../../components/common/LoadingState';
import { EmptyState } from '../../components/common/EmptyState';
import { TrendAreaChart } from '../../components/charts/TrendAreaChart';
import { AppointmentsBarChart } from '../../components/charts/AppointmentsBarChart';
import { useDashboardStats } from '../../hooks/useDashboardStats';
import { useRevenue } from '../../hooks/useAppointments';
import { useAuth } from '../../hooks/useAuth';
import { formatRupees } from '../../utils/currency';
import { REVENUE_RANGE_OPTIONS } from '../../constants/revenueRanges';
import type { RevenueRange } from '../../types/appointment';

function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
}

const quickActions = [
  {
    label: 'New Appointment',
    icon: EventAvailableRoundedIcon,
    to: '/appointments',
  },
  {
    label: 'Add Patient',
    icon: PersonAddRoundedIcon,
    to: '/patients',
  },
  {
    label: 'Ask AI Assistant',
    icon: SmartToyRoundedIcon,
    to: '/chat',
  },
  {
    label: 'View Analytics',
    icon: InsightsRoundedIcon,
    to: '/analytics',
  },
];

export function DashboardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { isLoading, stats, trends, appointmentsPerDay, revenueTrend, patientGrowth, appointments } =
    useDashboardStats();
  const [revenueRange, setRevenueRange] = useState<RevenueRange>('TODAY');
  const { data: revenue, isLoading: isRevenueLoading } = useRevenue(revenueRange);

  if (isLoading) {
    return <LoadingState label="Loading dashboard..." minHeight={400} />;
  }

  const recentAppointments = [...appointments]
    .sort((a, b) => b.scheduledStart.localeCompare(a.scheduledStart))
    .slice(0, 8);

  const displayName = user?.email?.split('@')[0] ?? 'there';

  return (
    <Box>
      <PageHeader
        title="Dashboard"
        subtitle="A snapshot of today's clinic activity and recent trends."
      />

      {/* Hero / welcome banner */}
      <Card
        elevation={0}
        sx={{
          mb: 2.5,
          position: 'relative',
          overflow: 'hidden',
          background: 'linear-gradient(135deg, #3B6FE0 0%, #2A50A8 55%, #0FB5A7 130%)',
          color: '#FFFFFF',
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            pointerEvents: 'none',
            background:
              'radial-gradient(circle at 90% 10%, rgba(255,255,255,0.16) 0%, transparent 45%), radial-gradient(circle at 10% 110%, rgba(255,255,255,0.12) 0%, transparent 45%)',
          }}
        />
        <CardContent sx={{ position: 'relative', p: { xs: 3, md: 4 } }}>
          <Stack
            direction={{ xs: 'column', lg: 'row' }}
            justifyContent="space-between"
            alignItems={{ xs: 'flex-start', lg: 'center' }}
            spacing={3}
          >
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                {getGreeting()}, {displayName} 👋
              </Typography>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 0.75, opacity: 0.9 }}>
                <CalendarMonthRoundedIcon fontSize="small" />
                <Typography variant="body2">{format(new Date(), 'EEEE, MMMM d, yyyy')}</Typography>
                {user?.role && (
                  <Chip
                    label={user.role}
                    size="small"
                    sx={{ bgcolor: 'rgba(255,255,255,0.18)', color: '#FFFFFF', fontWeight: 700, height: 22 }}
                  />
                )}
              </Stack>
              <Typography variant="body2" sx={{ mt: 1.5, opacity: 0.9, maxWidth: 480 }}>
                You have {stats.todaysAppointments} appointment{stats.todaysAppointments === 1 ? '' : 's'}{' '}
                scheduled today and {stats.upcomingAppointments.length} coming up next.
              </Typography>
            </Box>

            <Stack direction="row" spacing={1.5} flexWrap="wrap" useFlexGap>
              {quickActions.map(({ label, icon: Icon, to }) => (
                <Button
                  key={label}
                  onClick={() => navigate(to)}
                  startIcon={<Icon fontSize="small" />}
                  sx={{
                    bgcolor: 'rgba(255,255,255,0.14)',
                    color: '#FFFFFF',
                    fontWeight: 600,
                    px: 2,
                    '&:hover': { bgcolor: 'rgba(255,255,255,0.26)' },
                  }}
                >
                  {label}
                </Button>
              ))}
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Total Patients"
            value={stats.totalPatients}
            icon={<PeopleAltRoundedIcon />}
            color="primary"
            trendValue={trends.newPatientsTrend}
            helperText="new patients, 7 days"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Today's Appointments"
            value={stats.todaysAppointments}
            icon={<EventAvailableRoundedIcon />}
            color="secondary"
            trendValue={trends.appointmentsTrend}
            helperText="volume, 7 days"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Doctors"
            value={`${stats.activeDoctors}/${stats.totalDoctors}`}
            icon={<MedicalServicesRoundedIcon />}
            color="info"
            helperText="active of total"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Stack direction="row" alignItems="flex-start" justifyContent="space-between">
                <Typography variant="body2" color="text.secondary" fontWeight={600}>
                  Revenue
                </Typography>
                <Avatar
                  variant="rounded"
                  sx={{
                    bgcolor: (theme) => `${theme.palette.success.main}1F`,
                    color: 'success.main',
                    width: 46,
                    height: 46,
                    borderRadius: 2.5,
                  }}
                >
                  <PaidRoundedIcon />
                </Avatar>
              </Stack>
              <Typography variant="h4" sx={{ mt: 0.5 }}>
                {isRevenueLoading ? '—' : formatRupees(revenue?.totalRevenue ?? 0)}
              </Typography>
              <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mt: 1.5 }}>
                <Typography variant="caption" color="text.secondary">
                  {revenue?.completedAppointments ?? 0} completed visit
                  {revenue?.completedAppointments === 1 ? '' : 's'}
                </Typography>
                <Select
                  size="small"
                  value={revenueRange}
                  onChange={(e) => setRevenueRange(e.target.value as RevenueRange)}
                  variant="standard"
                  sx={{ fontSize: 13, fontWeight: 600, '&:before, &:after': { display: 'none' } }}
                >
                  {REVENUE_RANGE_OPTIONS.map((option) => (
                    <MenuItem key={option.value} value={option.value} sx={{ fontSize: 13 }}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader
              title="Appointments per day"
              subheader="Last 14 days"
              titleTypographyProps={{ variant: 'subtitle1' }}
            />
            <CardContent sx={{ pt: 0 }}>
              <AppointmentsBarChart data={appointmentsPerDay} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader
              title="Upcoming appointments"
              titleTypographyProps={{ variant: 'subtitle1' }}
              action={
                <Button
                  size="small"
                  endIcon={<ArrowForwardRoundedIcon fontSize="small" />}
                  onClick={() => navigate('/appointments')}
                >
                  View all
                </Button>
              }
            />
            <CardContent sx={{ pt: 0 }}>
              {stats.upcomingAppointments.length === 0 ? (
                <EmptyState title="No upcoming appointments" />
              ) : (
                <List disablePadding>
                  {stats.upcomingAppointments.map((appt) => (
                    <ListItem key={appt.id} disableGutters divider>
                      <ListItemAvatar>
                        <Avatar sx={{ bgcolor: 'primary.light' }}>
                          {appt.patientName?.charAt(0) ?? '?'}
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={appt.patientName}
                        secondary={`${appt.doctorName} · ${format(new Date(appt.scheduledStart), 'MMM d, h:mm a')}`}
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardHeader title="Patient growth" subheader="Cumulative, last 14 days" titleTypographyProps={{ variant: 'subtitle1' }} />
            <CardContent sx={{ pt: 0 }}>
              <TrendAreaChart data={patientGrowth} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardHeader
              title="Revenue"
              subheader="Consultation fees on completed appointments, last 14 days"
              titleTypographyProps={{ variant: 'subtitle1' }}
            />
            <CardContent sx={{ pt: 0 }}>
              <TrendAreaChart data={revenueTrend} color="#22A06B" valuePrefix="₹" />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        <CardHeader
          title="Recent appointments"
          titleTypographyProps={{ variant: 'subtitle1' }}
          action={
            <Button
              size="small"
              endIcon={<ArrowForwardRoundedIcon fontSize="small" />}
              onClick={() => navigate('/appointments')}
            >
              View all
            </Button>
          }
        />
        <CardContent sx={{ pt: 0 }}>
          {recentAppointments.length === 0 ? (
            <EmptyState title="No appointments yet" />
          ) : (
            <Stack sx={{ overflowX: 'auto' }}>
              <Box component="table" sx={{ width: '100%', borderCollapse: 'collapse', minWidth: 640 }}>
                <Box component="thead">
                  <Box component="tr">
                    {['Patient', 'Doctor', 'Date & time', 'Status'].map((header) => (
                      <Box
                        component="th"
                        key={header}
                        sx={{ textAlign: 'left', py: 1.25, px: 1, color: 'text.secondary', fontSize: 12, fontWeight: 700, borderBottom: '1px solid', borderColor: 'divider' }}
                      >
                        {header}
                      </Box>
                    ))}
                  </Box>
                </Box>
                <Box component="tbody">
                  {recentAppointments.map((appt) => (
                    <Box
                      component="tr"
                      key={appt.id}
                      sx={{
                        cursor: 'pointer',
                        transition: 'background-color 0.15s ease',
                        '&:hover': { bgcolor: 'action.hover' },
                      }}
                      onClick={() => navigate('/appointments')}
                    >
                      <Box component="td" sx={{ py: 1.25, px: 1, borderBottom: '1px solid', borderColor: 'divider', fontSize: 14 }}>
                        <Stack direction="row" spacing={1.25} alignItems="center">
                          <Avatar sx={{ width: 28, height: 28, fontSize: 13, bgcolor: 'primary.light' }}>
                            {appt.patientName?.charAt(0) ?? '?'}
                          </Avatar>
                          <span>{appt.patientName}</span>
                        </Stack>
                      </Box>
                      <Box component="td" sx={{ py: 1.25, px: 1, borderBottom: '1px solid', borderColor: 'divider', fontSize: 14 }}>
                        {appt.doctorName}
                      </Box>
                      <Box component="td" sx={{ py: 1.25, px: 1, borderBottom: '1px solid', borderColor: 'divider', fontSize: 14 }}>
                        {format(new Date(appt.scheduledStart), 'MMM d, yyyy h:mm a')}
                      </Box>
                      <Box component="td" sx={{ py: 1.25, px: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
                        <StatusChip status={appt.status} />
                      </Box>
                    </Box>
                  ))}
                </Box>
              </Box>
            </Stack>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
