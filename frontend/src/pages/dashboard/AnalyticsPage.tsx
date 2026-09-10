import { Box, Card, CardContent, CardHeader, Stack, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { PageHeader } from '../../components/common/PageHeader';
import { LoadingState } from '../../components/common/LoadingState';
import { TrendAreaChart } from '../../components/charts/TrendAreaChart';
import { AppointmentsBarChart } from '../../components/charts/AppointmentsBarChart';
import { useDashboardStats } from '../../hooks/useDashboardStats';
import { StatusChip } from '../../components/common/StatusChip';
import type { AppointmentStatus } from '../../types/appointment';

export function AnalyticsPage() {
  const { isLoading, appointments, appointmentsPerDay, revenueTrend, patientGrowth, doctors } =
    useDashboardStats();

  if (isLoading) {
    return <LoadingState label="Loading analytics..." minHeight={400} />;
  }

  const statusCounts = appointments.reduce<Record<string, number>>((acc, appt) => {
    acc[appt.status] = (acc[appt.status] ?? 0) + 1;
    return acc;
  }, {});

  const doctorLoad = doctors
    .map((doctor) => ({
      doctor,
      count: appointments.filter((a) => a.doctorId === doctor.id).length,
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 5);

  return (
    <Box>
      <PageHeader title="Analytics" subtitle="Deeper insight into appointments, growth, and revenue." />

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardHeader title="Appointments per day" titleTypographyProps={{ variant: 'subtitle1' }} />
            <CardContent sx={{ pt: 0 }}>
              <AppointmentsBarChart data={appointmentsPerDay} height={280} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardHeader title="Revenue trend" titleTypographyProps={{ variant: 'subtitle1' }} />
            <CardContent sx={{ pt: 0 }}>
              <TrendAreaChart data={revenueTrend} color="#22A06B" valuePrefix="₹" height={280} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardHeader title="Patient growth" titleTypographyProps={{ variant: 'subtitle1' }} />
            <CardContent sx={{ pt: 0 }}>
              <TrendAreaChart data={patientGrowth} height={280} />
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Appointment status breakdown" titleTypographyProps={{ variant: 'subtitle1' }} />
            <CardContent sx={{ pt: 0 }}>
              <Stack spacing={1.5}>
                {Object.entries(statusCounts).length === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    No appointment data yet.
                  </Typography>
                )}
                {Object.entries(statusCounts).map(([status, count]) => (
                  <Stack key={status} direction="row" alignItems="center" justifyContent="space-between">
                    <StatusChip status={status as AppointmentStatus} />
                    <Typography variant="body2" fontWeight={700}>
                      {count}
                    </Typography>
                  </Stack>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        <CardHeader title="Busiest doctors" subheader="By total booked appointments" titleTypographyProps={{ variant: 'subtitle1' }} />
        <CardContent sx={{ pt: 0 }}>
          <Stack spacing={2}>
            {doctorLoad.map(({ doctor, count }) => (
              <Stack key={doctor.id} direction="row" alignItems="center" spacing={2}>
                <Typography variant="body2" sx={{ minWidth: 180 }} fontWeight={600}>
                  Dr. {doctor.firstName} {doctor.lastName}
                </Typography>
                <Box
                  sx={{
                    flexGrow: 1,
                    height: 8,
                    borderRadius: 4,
                    bgcolor: 'action.hover',
                    overflow: 'hidden',
                  }}
                >
                  <Box
                    sx={{
                      height: '100%',
                      width: `${Math.min(100, (count / (doctorLoad[0]?.count || 1)) * 100)}%`,
                      bgcolor: 'primary.main',
                    }}
                  />
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ minWidth: 24, textAlign: 'right' }}>
                  {count}
                </Typography>
              </Stack>
            ))}
            {doctorLoad.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                No doctors or appointments to display yet.
              </Typography>
            )}
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
