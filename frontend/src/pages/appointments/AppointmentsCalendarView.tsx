import { useMemo, useState } from 'react';
import {
  Box,
  Card,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import ChevronLeftRoundedIcon from '@mui/icons-material/ChevronLeftRounded';
import ChevronRightRoundedIcon from '@mui/icons-material/ChevronRightRounded';
import TodayRoundedIcon from '@mui/icons-material/TodayRounded';
import {
  addMonths,
  eachDayOfInterval,
  endOfMonth,
  endOfWeek,
  format,
  isSameMonth,
  isToday,
  startOfMonth,
  startOfWeek,
  subMonths,
} from 'date-fns';
import type { Appointment } from '../../types/appointment';

interface AppointmentsCalendarViewProps {
  appointments: Appointment[];
  onSelectAppointment: (appointment: Appointment) => void;
}

const statusDotColor: Record<string, string> = {
  SCHEDULED: '#3B9FE0',
  CONFIRMED: '#3B6FE0',
  COMPLETED: '#22A06B',
  CANCELLED: '#E4574C',
  RESCHEDULED: '#E6A23C',
  NO_SHOW: '#9AA3B8',
};

export function AppointmentsCalendarView({ appointments, onSelectAppointment }: AppointmentsCalendarViewProps) {
  const [cursor, setCursor] = useState(new Date());

  const days = useMemo(() => {
    const start = startOfWeek(startOfMonth(cursor));
    const end = endOfWeek(endOfMonth(cursor));
    return eachDayOfInterval({ start, end });
  }, [cursor]);

  const appointmentsByDay = useMemo(() => {
    const map = new Map<string, Appointment[]>();
    for (const appointment of appointments) {
      const key = format(new Date(appointment.scheduledStart), 'yyyy-MM-dd');
      const list = map.get(key) ?? [];
      list.push(appointment);
      map.set(key, list);
    }
    return map;
  }, [appointments]);

  return (
    <Card sx={{ p: 2.5 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <Typography variant="h6">{format(cursor, 'MMMM yyyy')}</Typography>
        <Stack direction="row" spacing={0.5}>
          <IconButton size="small" onClick={() => setCursor(new Date())}>
            <TodayRoundedIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" onClick={() => setCursor((c) => subMonths(c, 1))}>
            <ChevronLeftRoundedIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" onClick={() => setCursor((c) => addMonths(c, 1))}>
            <ChevronRightRoundedIcon fontSize="small" />
          </IconButton>
        </Stack>
      </Stack>

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 1, mb: 1 }}>
        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
          <Typography key={d} variant="caption" color="text.secondary" fontWeight={700} textAlign="center">
            {d}
          </Typography>
        ))}
      </Box>

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 1 }}>
        {days.map((day) => {
          const key = format(day, 'yyyy-MM-dd');
          const dayAppointments = appointmentsByDay.get(key) ?? [];
          const inMonth = isSameMonth(day, cursor);

          return (
            <Box
              key={key}
              sx={{
                minHeight: 96,
                borderRadius: 2,
                border: '1px solid',
                borderColor: 'divider',
                p: 1,
                bgcolor: isToday(day) ? 'action.hover' : 'transparent',
                opacity: inMonth ? 1 : 0.4,
              }}
            >
              <Typography variant="caption" fontWeight={isToday(day) ? 800 : 600}>
                {format(day, 'd')}
              </Typography>
              <Stack spacing={0.4} sx={{ mt: 0.5 }}>
                {dayAppointments.slice(0, 3).map((appt) => (
                  <Tooltip
                    key={appt.id}
                    title={`${appt.patientName} · ${format(new Date(appt.scheduledStart), 'h:mm a')} · ${appt.doctorName}`}
                  >
                    <Box
                      onClick={() => onSelectAppointment(appt)}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 0.5,
                        px: 0.75,
                        py: 0.25,
                        borderRadius: 1,
                        fontSize: 11,
                        cursor: 'pointer',
                        bgcolor: 'action.hover',
                        '&:hover': { bgcolor: 'action.selected' },
                      }}
                    >
                      <Box
                        sx={{
                          width: 6,
                          height: 6,
                          borderRadius: '50%',
                          bgcolor: statusDotColor[appt.status] ?? 'grey.500',
                          flexShrink: 0,
                        }}
                      />
                      <Typography variant="caption" noWrap sx={{ fontSize: 11 }}>
                        {format(new Date(appt.scheduledStart), 'h:mm a')} {appt.patientName}
                      </Typography>
                    </Box>
                  </Tooltip>
                ))}
                {dayAppointments.length > 3 && (
                  <Typography variant="caption" color="text.secondary">
                    +{dayAppointments.length - 3} more
                  </Typography>
                )}
              </Stack>
            </Box>
          );
        })}
      </Box>
    </Card>
  );
}
