import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Alert,
  Box,
  Button,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import FavoriteRoundedIcon from '@mui/icons-material/FavoriteRounded';
import PlayCircleRoundedIcon from '@mui/icons-material/PlayCircleRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import BlockRoundedIcon from '@mui/icons-material/BlockRounded';
import { useRequestDemo } from '../../hooks/useEnquiry';
import type { ApiError } from '../../types/common';

const demoSchema = z.object({
  fullName: z.string().min(1, 'Your name is required'),
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  phone: z.string().min(1, 'Phone number is required'),
  clinicName: z.string().optional(),
  message: z.string().optional(),
});

type DemoFormValues = z.infer<typeof demoSchema>;

export function RequestDemoPage() {
  const requestDemo = useRequestDemo();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DemoFormValues>({ resolver: zodResolver(demoSchema) });

  const onSubmit = handleSubmit((values) => {
    requestDemo.mutate({
      fullName: values.fullName,
      email: values.email,
      phone: values.phone,
      clinicName: values.clinicName || undefined,
      message: values.message || undefined,
    });
  });

  const errorMessage = (requestDemo.error as ApiError | null)?.message;

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: '#F5F7FB',
        px: 3,
        py: 6,
      }}
    >
      <Paper
        elevation={0}
        sx={{
          width: '100%',
          maxWidth: 480,
          p: { xs: 3, sm: 4.5 },
          borderRadius: 4,
          border: '1px solid',
          borderColor: 'divider',
          boxShadow: '0 24px 60px -24px rgba(15,23,42,0.16)',
        }}
      >
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 3 }}>
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

        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
          <PlayCircleRoundedIcon color="primary" fontSize="small" />
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            Try a Live Demo
          </Typography>
        </Stack>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Leave your details and we'll drop you straight into a real, fully-populated clinic
          console - no setup, no waiting. It's a read-only preview; a member of our team will
          reach out afterwards.
        </Typography>

        <Stack direction="row" spacing={2.5} sx={{ mb: 3 }}>
          <Stack direction="row" spacing={0.75} alignItems="center">
            <VisibilityRoundedIcon fontSize="small" color="success" />
            <Typography variant="caption" color="text.secondary">
              See everything
            </Typography>
          </Stack>
          <Stack direction="row" spacing={0.75} alignItems="center">
            <BlockRoundedIcon fontSize="small" color="error" />
            <Typography variant="caption" color="text.secondary">
              Can't change anything
            </Typography>
          </Stack>
        </Stack>

        {errorMessage && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errorMessage}
          </Alert>
        )}

        <Box component="form" onSubmit={onSubmit} noValidate>
          <Stack spacing={2}>
            <TextField
              label="Full name"
              fullWidth
              autoFocus
              error={Boolean(errors.fullName)}
              helperText={errors.fullName?.message}
              {...register('fullName')}
            />
            <TextField
              label="Email"
              fullWidth
              error={Boolean(errors.email)}
              helperText={errors.email?.message}
              {...register('email')}
            />
            <TextField
              label="Phone"
              fullWidth
              error={Boolean(errors.phone)}
              helperText={errors.phone?.message}
              {...register('phone')}
            />
            <TextField label="Clinic name (optional)" fullWidth {...register('clinicName')} />
            <TextField
              label="Anything you'd like us to know? (optional)"
              fullWidth
              multiline
              minRows={2}
              {...register('message')}
            />

            <Button
              type="submit"
              variant="contained"
              size="large"
              fullWidth
              loading={requestDemo.isPending}
              sx={{ py: 1.25, fontWeight: 700, mt: 1 }}
            >
              Start My Free Demo
            </Button>
          </Stack>
        </Box>

        <Stack alignItems="center" sx={{ mt: 3 }}>
          <Typography variant="body2" color="text.secondary">
            Already have an account?{' '}
            <Link component={RouterLink} to="/login" underline="hover">
              Sign in
            </Link>
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
}
