import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Alert,
  Box,
  Button,
  CardContent,
  InputAdornment,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import { Link as RouterLink } from 'react-router-dom';
import { useState } from 'react';
import FavoriteRoundedIcon from '@mui/icons-material/FavoriteRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import MarkEmailReadRoundedIcon from '@mui/icons-material/MarkEmailReadRounded';
import { buildTheme } from '../../theme/theme';
import { useForgotPassword } from '../../hooks/usePasswordReset';
import type { ApiError } from '../../types/common';

const lightFormTheme = buildTheme('light');

const forgotPasswordSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
});

type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

export function ForgotPasswordPage() {
  const forgotPassword = useForgotPassword();
  const [submittedEmail, setSubmittedEmail] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({ resolver: zodResolver(forgotPasswordSchema) });

  const onSubmit = handleSubmit((values) => {
    forgotPassword.mutate(values, {
      onSuccess: () => setSubmittedEmail(values.email),
    });
  });

  const errorMessage = (forgotPassword.error as ApiError | null)?.message;

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

            {submittedEmail ? (
              <Stack spacing={2} alignItems="center" textAlign="center">
                <MarkEmailReadRoundedIcon sx={{ fontSize: 48, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  Check your email
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  If an account exists for <strong>{submittedEmail}</strong>, we've sent a link to reset your
                  password. It expires in 30 minutes.
                </Typography>
                <Button component={RouterLink} to="/login" variant="text" startIcon={<ArrowBackRoundedIcon />}>
                  Back to sign in
                </Button>
              </Stack>
            ) : (
              <>
                <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>
                  Forgot your password?
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Enter your email and we'll send you a link to reset it.
                </Typography>

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
                      autoFocus
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
                    <Button
                      type="submit"
                      variant="contained"
                      size="large"
                      fullWidth
                      loading={forgotPassword.isPending}
                      sx={{ py: 1.25, fontWeight: 700 }}
                    >
                      Send reset link
                    </Button>
                  </Stack>
                </Box>

                <Stack alignItems="center" sx={{ mt: 3 }}>
                  <Link component={RouterLink} to="/login" variant="body2" underline="hover">
                    Back to sign in
                  </Link>
                </Stack>
              </>
            )}
          </CardContent>
        </Paper>
      </Box>
    </ThemeProvider>
  );
}
