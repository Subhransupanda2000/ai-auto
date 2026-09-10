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
import ShieldRoundedIcon from '@mui/icons-material/ShieldRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import { useSuperAdminAuth } from '../../hooks/useSuperAdminAuth';
import type { ApiError } from '../../types/common';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

/**
 * Login for platform super admins only - a separate surface from the
 * clinic staff login ({@code LoginPage}), with its own session and its
 * own single purpose: onboarding new clinic tenants.
 */
export function SuperAdminLoginPage() {
  const { login, isLoggingIn, loginError } = useSuperAdminAuth();

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
        bgcolor: '#0B1220',
        px: 2,
        backgroundImage:
          'radial-gradient(circle at top left, rgba(59,111,224,0.18) 0%, transparent 45%), radial-gradient(circle at bottom right, rgba(15,181,167,0.14) 0%, transparent 45%)',
      }}
    >
      <Paper
        elevation={0}
        sx={{
          width: '100%',
          maxWidth: 420,
          borderRadius: 4,
          border: '1px solid',
          borderColor: 'rgba(255,255,255,0.08)',
          bgcolor: '#131A2B',
        }}
      >
        <CardContent sx={{ p: { xs: 3, sm: 5 } }}>
          <Stack spacing={1} alignItems="center" sx={{ mb: 4 }}>
            <Avatar sx={{ bgcolor: 'secondary.main', width: 52, height: 52 }}>
              <ShieldRoundedIcon />
            </Avatar>
            <Typography variant="h5" sx={{ color: '#FFFFFF' }}>
              Platform Admin
            </Typography>
            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.65)' }} textAlign="center">
              Sign in to onboard and manage clinic tenants.
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
                sx={{
                  '& .MuiInputBase-root': { color: '#FFFFFF' },
                  '& .MuiInputLabel-root': { color: 'rgba(255,255,255,0.6)' },
                  '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.2)' },
                }}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <EmailRoundedIcon fontSize="small" sx={{ color: 'rgba(255,255,255,0.5)' }} />
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
                sx={{
                  '& .MuiInputBase-root': { color: '#FFFFFF' },
                  '& .MuiInputLabel-root': { color: 'rgba(255,255,255,0.6)' },
                  '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.2)' },
                }}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockRoundedIcon fontSize="small" sx={{ color: 'rgba(255,255,255,0.5)' }} />
                      </InputAdornment>
                    ),
                  },
                }}
                {...register('password')}
              />
              <Button type="submit" variant="contained" color="secondary" size="large" fullWidth loading={isLoggingIn}>
                Sign in
              </Button>
            </Stack>
          </Box>
        </CardContent>
      </Paper>
    </Box>
  );
}
