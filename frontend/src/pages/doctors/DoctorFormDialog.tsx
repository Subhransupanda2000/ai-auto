import { useEffect, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import type { DoctorRequest, DoctorWithSchedule } from '../../types/doctor';

const WEEK_DAYS = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'] as const;

const doctorSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  specialty: z.string().min(1, 'Specialty is required'),
  email: z.string().email('Enter a valid email'),
  phoneNumber: z.string().min(6, 'Enter a valid phone number'),
  bio: z.string().optional(),
  active: z.boolean(),
  workingHoursStart: z.string(),
  workingHoursEnd: z.string(),
});

type DoctorFormValues = z.infer<typeof doctorSchema>;

interface DoctorFormDialogProps {
  open: boolean;
  doctor?: DoctorWithSchedule | null;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: DoctorRequest) => void;
}

export function DoctorFormDialog({ open, doctor, loading, onClose, onSubmit }: DoctorFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DoctorFormValues>({
    resolver: zodResolver(doctorSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      specialty: '',
      email: '',
      phoneNumber: '',
      bio: '',
      active: true,
      workingHoursStart: '09:00',
      workingHoursEnd: '17:00',
    },
  });

  const [selectedDays, setSelectedDays] = useState<string[]>(['MON', 'TUE', 'WED', 'THU', 'FRI']);

  useEffect(() => {
    if (open) {
      reset({
        firstName: doctor?.firstName ?? '',
        lastName: doctor?.lastName ?? '',
        specialty: doctor?.specialty ?? '',
        email: doctor?.email ?? '',
        phoneNumber: doctor?.phoneNumber ?? '',
        bio: doctor?.bio ?? '',
        active: doctor?.active ?? true,
        workingHoursStart: doctor?.workingHoursStart ?? '09:00',
        workingHoursEnd: doctor?.workingHoursEnd ?? '17:00',
      });
      setSelectedDays(doctor?.workingDays ?? ['MON', 'TUE', 'WED', 'THU', 'FRI']);
    }
  }, [open, doctor, reset]);

  const toggleDay = (day: string) => {
    setSelectedDays((prev) => (prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]));
  };

  const submit = handleSubmit((values) => {
    onSubmit({ ...values, workingDays: selectedDays });
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{doctor ? 'Edit Doctor' : 'Create Doctor'}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="firstName"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="First name" fullWidth error={Boolean(errors.firstName)} helperText={errors.firstName?.message} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="lastName"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Last name" fullWidth error={Boolean(errors.lastName)} helperText={errors.lastName?.message} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="specialty"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Specialty" fullWidth error={Boolean(errors.specialty)} helperText={errors.specialty?.message} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Email" fullWidth error={Boolean(errors.email)} helperText={errors.email?.message} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="phoneNumber"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Phone number" fullWidth error={Boolean(errors.phoneNumber)} helperText={errors.phoneNumber?.message} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="active"
              control={control}
              render={({ field }) => (
                <FormControlLabel
                  sx={{ mt: 1 }}
                  control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                  label="Active"
                />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="bio"
              control={control}
              render={({ field }) => <TextField {...field} label="Bio" fullWidth multiline minRows={2} />}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="workingHoursStart"
              control={control}
              render={({ field }) => (
                <TextField {...field} type="time" label="Working hours start" fullWidth slotProps={{ inputLabel: { shrink: true } }} />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="workingHoursEnd"
              control={control}
              render={({ field }) => (
                <TextField {...field} type="time" label="Working hours end" fullWidth slotProps={{ inputLabel: { shrink: true } }} />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Typography variant="caption" color="text.secondary">
              Working days
            </Typography>
            <Stack direction="row" spacing={1} sx={{ mt: 0.5, flexWrap: 'wrap', gap: 1 }}>
              {WEEK_DAYS.map((day) => (
                <Chip
                  key={day}
                  label={day}
                  color={selectedDays.includes(day) ? 'primary' : 'default'}
                  variant={selectedDays.includes(day) ? 'filled' : 'outlined'}
                  onClick={() => toggleDay(day)}
                />
              ))}
            </Stack>
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          Cancel
        </Button>
        <Button onClick={submit} variant="contained" loading={loading}>
          {doctor ? 'Save changes' : 'Create doctor'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
