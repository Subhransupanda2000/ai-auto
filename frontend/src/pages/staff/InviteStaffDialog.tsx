import { useEffect, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  InputAdornment,
  MenuItem,
  TextField,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import VisibilityOffRoundedIcon from '@mui/icons-material/VisibilityOffRounded';
import type { RegisterRequest, Role } from '../../types/auth';

const ROLE_OPTIONS: { value: Role; label: string }[] = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'DOCTOR', label: 'Doctor' },
  { value: 'RECEPTIONIST', label: 'Receptionist' },
];

const staffSchema = z.object({
  fullName: z.string().min(1, 'Full name is required'),
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  role: z.enum(['ADMIN', 'DOCTOR', 'RECEPTIONIST']),
});

type StaffFormValues = z.infer<typeof staffSchema>;

interface InviteStaffDialogProps {
  open: boolean;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: RegisterRequest) => void;
}

export function InviteStaffDialog({ open, loading, onClose, onSubmit }: InviteStaffDialogProps) {
  const [showPassword, setShowPassword] = useState(false);
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<StaffFormValues>({
    resolver: zodResolver(staffSchema),
    defaultValues: { fullName: '', email: '', password: '', role: 'RECEPTIONIST' },
  });

  useEffect(() => {
    if (open) {
      reset({ fullName: '', email: '', password: '', role: 'RECEPTIONIST' });
      setShowPassword(false);
    }
  }, [open, reset]);

  const submit = handleSubmit((values) => onSubmit(values));

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Invite staff member</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={12}>
            <Controller
              name="fullName"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Full name"
                  fullWidth
                  autoFocus
                  error={Boolean(errors.fullName)}
                  helperText={errors.fullName?.message}
                />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Email"
                  fullWidth
                  error={Boolean(errors.email)}
                  helperText={errors.email?.message}
                />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="role"
              control={control}
              render={({ field }) => (
                <TextField {...field} select label="Role" fullWidth>
                  {ROLE_OPTIONS.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="password"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  type={showPassword ? 'text' : 'password'}
                  label="Temporary password"
                  fullWidth
                  error={Boolean(errors.password)}
                  helperText={errors.password?.message ?? 'Share this with them so they can sign in and change it.'}
                  slotProps={{
                    input: {
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton
                            size="small"
                            onClick={() => setShowPassword((prev) => !prev)}
                            edge="end"
                            aria-label={showPassword ? 'Hide password' : 'Show password'}
                          >
                            {showPassword ? (
                              <VisibilityOffRoundedIcon fontSize="small" />
                            ) : (
                              <VisibilityRoundedIcon fontSize="small" />
                            )}
                          </IconButton>
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              )}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          Cancel
        </Button>
        <Button onClick={submit} variant="contained" loading={loading}>
          Send invite
        </Button>
      </DialogActions>
    </Dialog>
  );
}
