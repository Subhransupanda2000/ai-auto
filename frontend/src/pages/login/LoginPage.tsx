import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Alert,
  Avatar,
  AvatarGroup,
  Box,
  Button,
  Checkbox,
  Divider,
  FormControlLabel,
  IconButton,
  InputAdornment,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useState } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { Link as RouterLink } from 'react-router-dom';
import FavoriteRoundedIcon from '@mui/icons-material/FavoriteRounded';
import MonitorHeartRoundedIcon from '@mui/icons-material/MonitorHeartRounded';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import VisibilityOffRoundedIcon from '@mui/icons-material/VisibilityOffRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import DescriptionRoundedIcon from '@mui/icons-material/DescriptionRounded';
import BarChartRoundedIcon from '@mui/icons-material/BarChartRounded';
import ShieldRoundedIcon from '@mui/icons-material/ShieldRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import StarRoundedIcon from '@mui/icons-material/StarRounded';
import VerifiedRoundedIcon from '@mui/icons-material/VerifiedRounded';
import LockPersonRoundedIcon from '@mui/icons-material/LockPersonRounded';
import { useAuth } from '../../hooks/useAuth';
import { buildTheme } from '../../theme/theme';
import type { ApiError } from '../../types/common';

const lightFormTheme = buildTheme('light');

// Overrides the browser's default autofill highlight (blue/yellow box) so
// autofilled fields keep matching the form's own white/light styling.
const autofillSx = {
  '& input:-webkit-autofill': {
    WebkitBoxShadow: '0 0 0 100px #FFFFFF inset',
    WebkitTextFillColor: '#1B2333',
    caretColor: '#1B2333',
    borderRadius: 'inherit',
  },
};

const loginSchema = z.object({
  email: z.string().min(1, 'Email or username is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const highlights = [
  { icon: EventAvailableRoundedIcon, label: 'Appointment\nScheduling' },
  { icon: GroupsRoundedIcon, label: 'Patient\nManagement' },
  { icon: DescriptionRoundedIcon, label: 'Digital Health\nRecords' },
  { icon: BarChartRoundedIcon, label: 'Reports &\nAnalytics' },
];

export function LoginPage() {
  const { login, isLoggingIn, loginError } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  const onSubmit = handleSubmit((values) => {
    login({ email: values.email, password: values.password }).catch(() => undefined);
  });

  const errorMessage = (loginError as ApiError | null)?.message;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        bgcolor: '#F5F7FB',
      }}
    >
      {/* Left branding panel */}
      <Box
        sx={{
          display: { xs: 'none', md: 'flex' },
          flexBasis: '50%',
          position: 'relative',
          overflow: 'hidden',
          flexDirection: 'column',
          justifyContent: 'space-between',
          p: { md: 6, lg: 8 },
          color: '#0B1B3A',
          background:
            'linear-gradient(160deg, #EAF2FF 0%, #E3F7F4 45%, #F3FBFA 100%)',
        }}
      >
        {/* Decorative blobs */}
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            pointerEvents: 'none',
            background:
              'radial-gradient(circle at 15% 20%, rgba(59,111,224,0.16) 0%, transparent 40%), radial-gradient(circle at 85% 75%, rgba(15,181,167,0.18) 0%, transparent 45%)',
          }}
        />

        <Box sx={{ position: 'relative', zIndex: 1 }}>
          <Typography
            variant="overline"
            sx={{ letterSpacing: 3, color: 'primary.main', fontWeight: 700 }}
          >
            Clinic Management System
          </Typography>
          <Typography
            variant="h3"
            sx={{ fontWeight: 800, mt: 1.5, lineHeight: 1.15, color: '#0B1B3A' }}
          >
            Better Care.
            <br />
            Smarter Management.
          </Typography>
          <Typography variant="body1" sx={{ mt: 2, maxWidth: 420, color: 'text.secondary' }}>
            Streamline your clinic operations, manage patients, appointments, and
            records — all in one place.
          </Typography>
        </Box>

        {/* Illustration */}
        <Box
          sx={{
            position: 'relative',
            zIndex: 1,
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            my: 4,
          }}
        >
          <Box
            sx={{
              width: '100%',
              maxWidth: 380,
              aspectRatio: '1',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background:
                'linear-gradient(135deg, rgba(59,111,224,0.12), rgba(15,181,167,0.12))',
              border: '1px solid rgba(59,111,224,0.15)',
            }}
          >
            <Box
              sx={{
                width: '68%',
                aspectRatio: '1',
                borderRadius: '50%',
                bgcolor: '#FFFFFF',
                boxShadow: '0 20px 60px -20px rgba(59,111,224,0.35)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <MonitorHeartRoundedIcon sx={{ fontSize: 96, color: 'primary.main' }} />
            </Box>
            <Box
              sx={{
                position: 'absolute',
                top: '12%',
                right: '10%',
                width: 56,
                height: 56,
                borderRadius: '50%',
                bgcolor: '#FFFFFF',
                boxShadow: '0 12px 30px -10px rgba(15,181,167,0.4)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <FavoriteRoundedIcon sx={{ color: 'secondary.main' }} />
            </Box>
            <Box
              sx={{
                position: 'absolute',
                bottom: '14%',
                left: '8%',
                width: 48,
                height: 48,
                borderRadius: '50%',
                bgcolor: '#FFFFFF',
                boxShadow: '0 12px 30px -10px rgba(59,111,224,0.4)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <ShieldRoundedIcon fontSize="small" sx={{ color: 'primary.main' }} />
            </Box>
          </Box>
        </Box>

        {/* Feature chips */}
        <Stack
          direction="row"
          spacing={{ xs: 2, lg: 3 }}
          sx={{ position: 'relative', zIndex: 1 }}
        >
          {highlights.map(({ icon: Icon, label }) => (
            <Stack key={label} spacing={1} alignItems="center" sx={{ flex: 1, minWidth: 0 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: '50%',
                  bgcolor: 'rgba(255,255,255,0.75)',
                  border: '1px solid rgba(59,111,224,0.15)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Icon fontSize="small" sx={{ color: 'primary.main' }} />
              </Box>
              <Typography
                variant="caption"
                textAlign="center"
                sx={{ whiteSpace: 'pre-line', color: 'text.secondary', fontWeight: 600, lineHeight: 1.3 }}
              >
                {label}
              </Typography>
            </Stack>
          ))}
        </Stack>
      </Box>

      {/* Right form panel - always rendered with the light theme so it stays white
          and legible even when the app is switched to dark mode. */}
      <ThemeProvider theme={lightFormTheme}>
      <Box
        sx={{
          flexBasis: { xs: '100%', md: '50%' },
          position: 'relative',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: '#FFFFFF',
          px: 3,
          py: 6,
        }}
      >
        {/* Subtle background texture so the panel doesn't read as bare white
            space - a soft dot grid plus two faint color washes echoing the
            left panel's palette. */}
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            pointerEvents: 'none',
            opacity: 0.5,
            backgroundImage:
              'radial-gradient(rgba(59,111,224,0.14) 1px, transparent 1px)',
            backgroundSize: '28px 28px',
            maskImage: 'radial-gradient(circle at 50% 40%, rgba(0,0,0,0.55) 0%, transparent 70%)',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            pointerEvents: 'none',
            background:
              'radial-gradient(circle at 85% 8%, rgba(15,181,167,0.10) 0%, transparent 40%), radial-gradient(circle at 8% 92%, rgba(59,111,224,0.10) 0%, transparent 40%)',
          }}
        />

        {/* Floating accent badges - only where there's room to spare. */}
        <Stack
          alignItems="center"
          spacing={1}
          sx={{
            display: { xs: 'none', lg: 'flex' },
            position: 'absolute',
            top: '14%',
            right: '10%',
          }}
        >
          <Avatar sx={{ width: 46, height: 46, bgcolor: '#FFFFFF', boxShadow: '0 12px 28px -10px rgba(15,181,167,0.45)' }}>
            <VerifiedRoundedIcon sx={{ color: 'secondary.main' }} />
          </Avatar>
          <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary' }}>
            Verified clinics
          </Typography>
        </Stack>
        <Stack
          alignItems="center"
          spacing={1}
          sx={{
            display: { xs: 'none', lg: 'flex' },
            position: 'absolute',
            bottom: '16%',
            left: '9%',
          }}
        >
          <Avatar sx={{ width: 46, height: 46, bgcolor: '#FFFFFF', boxShadow: '0 12px 28px -10px rgba(59,111,224,0.45)' }}>
            <LockPersonRoundedIcon sx={{ color: 'primary.main' }} />
          </Avatar>
          <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary' }}>
            End-to-end secure
          </Typography>
        </Stack>

        <Paper
          elevation={0}
          sx={{
            position: 'relative',
            zIndex: 1,
            width: '100%',
            maxWidth: 400,
            p: { xs: 3, sm: 4.5 },
            borderRadius: 4,
            border: '1px solid',
            borderColor: 'divider',
            boxShadow: '0 24px 60px -24px rgba(15,23,42,0.16)',
          }}
        >
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

          <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>
            Welcome Back
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Sign in to your account to continue
          </Typography>

          {errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Alert>
          )}

          <Box component="form" onSubmit={onSubmit} noValidate>
            <Stack spacing={2.5}>
              <TextField
                label="Email or Username"
                autoComplete="username"
                fullWidth
                sx={autofillSx}
                error={Boolean(errors.email)}
                helperText={errors.email?.message}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <PersonRoundedIcon fontSize="small" color="action" />
                      </InputAdornment>
                    ),
                  },
                }}
                {...register('email')}
              />
              <TextField
                label="Password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                fullWidth
                sx={autofillSx}
                error={Boolean(errors.password)}
                helperText={errors.password?.message}
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
                {...register('password')}
              />

              <Stack direction="row" alignItems="center" justifyContent="space-between">
                <FormControlLabel
                  control={
                    <Checkbox
                      size="small"
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                    />
                  }
                  label={<Typography variant="body2">Remember me</Typography>}
                />
                <Link component={RouterLink} to="/forgot-password" variant="body2" underline="hover">
                  Forgot password?
                </Link>
              </Stack>

              <Button
                type="submit"
                variant="contained"
                size="large"
                fullWidth
                loading={isLoggingIn}
                endIcon={<ArrowForwardRoundedIcon />}
                sx={{ py: 1.25, fontWeight: 700 }}
              >
                Sign In
              </Button>
            </Stack>
          </Box>

          <Divider sx={{ my: 3 }}>
            <Typography variant="caption" color="text.secondary">
              or
            </Typography>
          </Divider>

          <Button
            variant="outlined"
            color="inherit"
            fullWidth
            size="large"
            disabled
            title="Single sign-on is coming soon"
            sx={{ py: 1.25, borderColor: 'divider', color: 'text.secondary' }}
          >
            Continue with Microsoft (coming soon)
          </Button>

          <Stack alignItems="center" sx={{ mt: 2.5 }}>
            <Typography variant="body2" color="text.secondary">
              New here?{' '}
              <Link component={RouterLink} to="/request-demo" underline="hover">
                Try a live demo
              </Link>
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
              Requested access already? Our admin will contact you shortly.
            </Typography>
          </Stack>

          <Stack alignItems="center" spacing={0.5} sx={{ mt: 3 }}>
            <Stack direction="row" spacing={0.5} alignItems="center">
              <ShieldRoundedIcon sx={{ fontSize: 14, color: 'text.disabled' }} />
              <Typography variant="caption" color="text.disabled">
                Your data is safe with us
              </Typography>
            </Stack>
            <Typography variant="caption" color="text.disabled">
              HIPAA Compliant &nbsp;•&nbsp; Secure &nbsp;•&nbsp; Trusted
            </Typography>
          </Stack>
        </Paper>

        {/* Social proof strip - fills the remaining space below the card
            instead of leaving it bare. */}
        <Stack
          alignItems="center"
          spacing={1}
          sx={{ position: 'relative', zIndex: 1, width: '100%', maxWidth: 400, mt: 4 }}
        >
          <Stack direction="row" spacing={0.5}>
            {Array.from({ length: 5 }).map((_, i) => (
              <StarRoundedIcon key={i} sx={{ fontSize: 16, color: '#F0B958' }} />
            ))}
          </Stack>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <AvatarGroup
              max={4}
              sx={{
                '& .MuiAvatar-root': {
                  width: 28,
                  height: 28,
                  fontSize: 12,
                  border: '2px solid #FFFFFF',
                },
              }}
            >
              <Avatar sx={{ bgcolor: 'primary.main' }}>SR</Avatar>
              <Avatar sx={{ bgcolor: 'secondary.main' }}>KP</Avatar>
              <Avatar sx={{ bgcolor: '#E4574C' }}>MJ</Avatar>
              <Avatar sx={{ bgcolor: '#E6A23C' }}>AT</Avatar>
            </AvatarGroup>
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
              Trusted by 500+ clinics worldwide
            </Typography>
          </Stack>
        </Stack>
      </Box>
      </ThemeProvider>
    </Box>
  );
}
