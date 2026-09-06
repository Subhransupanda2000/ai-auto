import {
  Avatar,
  Box,
  Card,
  CardContent,
  CardHeader,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Stack,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import MedicalServicesRoundedIcon from '@mui/icons-material/MedicalServicesRounded';
import PaidRoundedIcon from '@mui/icons-material/PaidRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { StatCard } from '../../components/common/StatCard';
import { StatusChip } from '../../components/common/StatusChip';
import { LoadingState } from '../../components/common/LoadingState';
import { EmptyState } from '../../components/common/EmptyState';
import { TrendAreaChart } from '../../components/charts/TrendAreaChart';
import { AppointmentsBarChart } from '../../components/charts/AppointmentsBarChart';
import { useDashboardStats } from '../../hooks/useDashboardStats';

export function DashboardPage() {
  const { isLoading, stats, appointmentsPerDay, revenueTrend, patientGrowth, appointments } =
    useDashboardStats();

  if (isLoading) {
    return <LoadingState label="Loading dashboard..." minHeight={400} />;
  }

  const recentAppointments = [...appointments]
    .sort((a, b) => b.scheduledStart.localeCompare(a.scheduledStart))
    .slice(0, 8);

  return (
    <Box>
      <PageHeader
        title="Dashboard"
        subtitle="A snapshot of today's clinic activity and recent trends."
      />

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Total Patients"
            value={stats.totalPatients}
            icon={<PeopleAltRoundedIcon />}
            color="primary"
            helperText="registered patients"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Today's Appointments"
            value={stats.todaysAppointments}
            icon={<EventAvailableRoundedIcon />}
            color="secondary"
            helperText="scheduled today"
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
          <StatCard
            label="Revenue (est.)"
            value={`$${stats.estimatedRevenue.toLocaleString()}`}
            icon={<PaidRoundedIcon />}
            color="success"
            helperText="from completed visits"
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
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
            <CardHeader title="Upcoming appointments" titleTypographyProps={{ variant: 'subtitle1' }} />
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
              subheader="Estimated, based on completed appointments"
              titleTypographyProps={{ variant: 'subtitle1' }}
            />
            <CardContent sx={{ pt: 0 }}>
              <TrendAreaChart data={revenueTrend} color="#22A06B" valuePrefix="$" />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        <CardHeader title="Recent appointments" titleTypographyProps={{ variant: 'subtitle1' }} />
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
                    <Box component="tr" key={appt.id}>
                      <Box component="td" sx={{ py: 1.25, px: 1, borderBottom: '1px solid', borderColor: 'divider', fontSize: 14 }}>
                        {appt.patientName}
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
