import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Alert,
  Box,
  Button,
  CardContent,
  IconButton,
  InputAdornment,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom';
import { useState } from 'react';
import FavoriteRoundedIcon from '@mui/icons-material/FavoriteRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import VisibilityOffRoundedIcon from '@mui/icons-material/VisibilityOffRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import { buildTheme } from '../../theme/theme';
import { useResetPassword } from '../../hooks/usePasswordReset';
import type { ApiError } from '../../types/common';

const lightFormTheme = buildTheme('light');

const resetPasswordSchema = z
  .object({
    newPassword: z.string().min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  });

type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') ?? '';
  const navigate = useNavigate();
  const resetPassword = useResetPassword();
  const [showPassword, setShowPassword] = useState(false);
  const [succeeded, setSucceeded] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({ resolver: zodResolver(resetPasswordSchema) });

  const onSubmit = handleSubmit((values) => {
    resetPassword.mutate({ token, newPassword: values.newPassword }, { onSuccess: () => setSucceeded(true) });
  });

  const errorMessage = (resetPassword.error as ApiError | null)?.message;

  return (
    <ThemeProvider theme={lightFormTheme}>
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: '#F5F7FB',
          px: 2,
          backgroundImage:
            'radial-gradient(circle at top left, #E8EFFE 0%, transparent 45%), radial-gradient(circle at bottom right, #E5FBF8 0%, transparent 45%)',
        }}
      >
        <Paper elevation={0} sx={{ width: '100%', maxWidth: 420, borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
          <CardContent sx={{ p: { xs: 3, sm: 5 } }}>
            <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 4 }}>
              <Box
                sx={{
                  width: 44,
                  height: 44,
                  borderRadius: '14px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: 'linear-gradient(135deg, #3B6FE0, #0FB5A7)',
                  boxShadow: '0 8px 20px -6px rgba(59,111,224,0.5)',
                }}
              >
                <FavoriteRoundedIcon sx={{ color: '#FFFFFF' }} fontSize="small" />
              </Box>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 800, lineHeight: 1.2 }}>
                  Care<Box component="span" sx={{ color: 'primary.main' }}>Nexa</Box>
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Clinic Management System
                </Typography>
              </Box>
            </Stack>

            {succeeded ? (
              <Stack spacing={2} alignItems="center" textAlign="center">
                <CheckCircleRoundedIcon sx={{ fontSize: 48, color: 'success.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  Password updated
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Your password has been reset. You can now sign in with your new password.
                </Typography>
                <Button variant="contained" fullWidth onClick={() => navigate('/login', { replace: true })}>
                  Go to sign in
                </Button>
              </Stack>
            ) : !token ? (
              <Alert severity="error">
                This reset link is missing its token. Request a new one from the{' '}
                <Link component={RouterLink} to="/forgot-password">
                  forgot password
                </Link>{' '}
                page.
              </Alert>
            ) : (
              <>
                <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>
                  Choose a new password
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Make sure it's at least 8 characters.
                </Typography>

                {errorMessage && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {errorMessage}
                  </Alert>
                )}

                <Box component="form" onSubmit={onSubmit} noValidate>
                  <Stack spacing={2.5}>
                    <TextField
                      label="New password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      fullWidth
                      autoFocus
                      error={Boolean(errors.newPassword)}
                      helperText={errors.newPassword?.message}
                      slotProps={{
                        input: {
                          startAdornment: (
                            <InputAdornment position="start">
                              <LockRoundedIcon fontSize="small" color="action" />
                            </InputAdornment>
                          ),
                          endAdornment: (
                            <InputAdornment position="end">
                              <IconButton
                                size="small"
                                onClick={() => setShowPassword((prev) => !prev)}
                                edge="end"
                                aria-label={showPassword ? 'Hide password' : 'Show password'}
                              >
                                {showPassword ? <VisibilityOffRoundedIcon fontSize="small" /> : <VisibilityRoundedIcon fontSize="small" />}
                              </IconButton>
                            </InputAdornment>
                          ),
                        },
                      }}
                      {...register('newPassword')}
                    />
                    <TextField
                      label="Confirm new password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      fullWidth
                      error={Boolean(errors.confirmPassword)}
                      helperText={errors.confirmPassword?.message}
                      slotProps={{
                        input: {
                          startAdornment: (
                            <InputAdornment position="start">
                              <LockRoundedIcon fontSize="small" color="action" />
                            </InputAdornment>
                          ),
                        },
                      }}
                      {...register('confirmPassword')}
                    />
                    <Button
                      type="submit"
                      variant="contained"
                      size="large"
                      fullWidth
                      loading={resetPassword.isPending}
                      sx={{ py: 1.25, fontWeight: 700 }}
                    >
                      Reset password
                    </Button>
                  </Stack>
                </Box>
              </>
            )}
          </CardContent>
        </Paper>
      </Box>
    </ThemeProvider>
  );
}
