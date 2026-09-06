import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Alert,
  Avatar,
  Box,
  Button,
  CardContent,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import LocalHospitalRoundedIcon from '@mui/icons-material/LocalHospitalRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import { useAuth } from '../../hooks/useAuth';
import type { ApiError } from '../../types/common';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { login, isLoggingIn, loginError } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  const onSubmit = handleSubmit((values) => {
    login(values).catch(() => undefined);
  });

  const errorMessage = (loginError as ApiError | null)?.message;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        px: 2,
        backgroundImage: (theme) =>
          theme.palette.mode === 'light'
            ? 'radial-gradient(circle at top left, #E8EFFE 0%, transparent 45%), radial-gradient(circle at bottom right, #E5FBF8 0%, transparent 45%)'
            : 'none',
      }}
    >
      <Paper
        elevation={0}
        sx={{ width: '100%', maxWidth: 420, borderRadius: 4, border: '1px solid', borderColor: 'divider' }}
      >
        <CardContent sx={{ p: { xs: 3, sm: 5 } }}>
          <Stack spacing={1} alignItems="center" sx={{ mb: 4 }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 52, height: 52 }}>
              <LocalHospitalRoundedIcon />
            </Avatar>
            <Typography variant="h5">HealthcareAI Console</Typography>
            <Typography variant="body2" color="text.secondary" textAlign="center">
              Sign in with your staff credentials to manage patients, appointments, and the AI receptionist.
            </Typography>
          </Stack>

          {errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Alert>
          )}

          <Box component="form" onSubmit={onSubmit} noValidate>
            <Stack spacing={2.5}>
              <TextField
                label="Email"
                type="email"
                autoComplete="email"
                fullWidth
                error={Boolean(errors.email)}
                helperText={errors.email?.message}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <EmailRoundedIcon fontSize="small" color="action" />
                      </InputAdornment>
                    ),
                  },
                }}
                {...register('email')}
              />
              <TextField
                label="Password"
                type="password"
                autoComplete="current-password"
                fullWidth
                error={Boolean(errors.password)}
                helperText={errors.password?.message}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockRoundedIcon fontSize="small" color="action" />
                      </InputAdornment>
                    ),
                  },
                }}
                {...register('password')}
              />
              <Button type="submit" variant="contained" size="large" fullWidth loading={isLoggingIn}>
                Sign in
              </Button>
            </Stack>
          </Box>
        </CardContent>
      </Paper>
    </Box>
  );
}
