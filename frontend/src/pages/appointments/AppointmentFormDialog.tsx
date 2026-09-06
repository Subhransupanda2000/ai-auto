import { useEffect, useMemo } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Autocomplete,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { addMinutes, format } from 'date-fns';
import { usePatients } from '../../hooks/usePatients';
import { useDoctors } from '../../hooks/useDoctors';
import type { Appointment, AppointmentRequest } from '../../types/appointment';

const appointmentSchema = z.object({
  patientId: z.string().min(1, 'Select a patient'),
  doctorId: z.string().min(1, 'Select a doctor'),
  date: z.string().min(1, 'Select a date'),
  startTime: z.string().min(1, 'Select a start time'),
  durationMinutes: z.coerce.number().min(15).max(240),
  reason: z.string().optional(),
});

type AppointmentFormValues = z.infer<typeof appointmentSchema>;

interface AppointmentFormDialogProps {
  open: boolean;
  appointment?: Appointment | null;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: AppointmentRequest) => void;
}

function toDateInput(iso?: string): string {
  return iso ? format(new Date(iso), 'yyyy-MM-dd') : format(new Date(), 'yyyy-MM-dd');
}

function toTimeInput(iso?: string): string {
  return iso ? format(new Date(iso), 'HH:mm') : '09:00';
}

export function AppointmentFormDialog({
  open,
  appointment,
  loading,
  onClose,
  onSubmit,
}: AppointmentFormDialogProps) {
  const { data: patients = [] } = usePatients();
  const { data: doctors = [] } = useDoctors();

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AppointmentFormValues>({
    resolver: zodResolver(appointmentSchema),
    defaultValues: {
      patientId: '',
      doctorId: '',
      date: format(new Date(), 'yyyy-MM-dd'),
      startTime: '09:00',
      durationMinutes: 30,
      reason: '',
    },
  });

  useEffect(() => {
    if (open) {
      const durationMinutes = appointment
        ? Math.round((new Date(appointment.scheduledEnd).getTime() - new Date(appointment.scheduledStart).getTime()) / 60000)
        : 30;
      reset({
        patientId: appointment?.patientId ?? '',
        doctorId: appointment?.doctorId ?? '',
        date: toDateInput(appointment?.scheduledStart),
        startTime: toTimeInput(appointment?.scheduledStart),
        durationMinutes,
        reason: appointment?.reason ?? '',
      });
    }
  }, [open, appointment, reset]);

  const patientOptions = useMemo(
    () => patients.map((p) => ({ id: p.id, label: `${p.firstName} ${p.lastName}` })),
    [patients],
  );
  const doctorOptions = useMemo(
    () => doctors.map((d) => ({ id: d.id, label: `Dr. ${d.firstName} ${d.lastName} (${d.specialty})` })),
    [doctors],
  );

  const submit = handleSubmit((values) => {
    const start = new Date(`${values.date}T${values.startTime}:00`);
    const end = addMinutes(start, values.durationMinutes);
    onSubmit({
      patientId: values.patientId,
      doctorId: values.doctorId,
      start: start.toISOString(),
      end: end.toISOString(),
      reason: values.reason,
    });
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{appointment ? 'Reschedule Appointment' : 'Book Appointment'}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={12}>
            <Controller
              name="patientId"
              control={control}
              render={({ field }) => (
                <Autocomplete
                  options={patientOptions}
                  disabled={Boolean(appointment)}
                  value={patientOptions.find((o) => o.id === field.value) ?? null}
                  onChange={(_e, value) => field.onChange(value?.id ?? '')}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Patient"
                      error={Boolean(errors.patientId)}
                      helperText={errors.patientId?.message}
                    />
                  )}
                />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="doctorId"
              control={control}
              render={({ field }) => (
                <Autocomplete
                  options={doctorOptions}
                  value={doctorOptions.find((o) => o.id === field.value) ?? null}
                  onChange={(_e, value) => field.onChange(value?.id ?? '')}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Doctor"
                      error={Boolean(errors.doctorId)}
                      helperText={errors.doctorId?.message}
                    />
                  )}
                />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="date"
              control={control}
              render={({ field }) => (
                <TextField {...field} type="date" label="Date" fullWidth slotProps={{ inputLabel: { shrink: true } }} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="startTime"
              control={control}
              render={({ field }) => (
                <TextField {...field} type="time" label="Start time" fullWidth slotProps={{ inputLabel: { shrink: true } }} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="durationMinutes"
              control={control}
              render={({ field }) => (
                <TextField {...field} type="number" label="Duration (minutes)" fullWidth />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="reason"
              control={control}
              render={({ field }) => <TextField {...field} label="Reason" fullWidth multiline minRows={2} />}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          Cancel
        </Button>
        <Button onClick={submit} variant="contained" loading={loading}>
          {appointment ? 'Save changes' : 'Book appointment'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
