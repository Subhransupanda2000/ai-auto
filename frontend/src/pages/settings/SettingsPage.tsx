import { useEffect, useState } from 'react';
import { useSnackbar } from 'notistack';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Chip,
  Divider,
  FormControlLabel,
  MenuItem,
  Slider,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import SaveRoundedIcon from '@mui/icons-material/SaveRounded';
import DarkModeRoundedIcon from '@mui/icons-material/DarkModeRounded';
import LightModeRoundedIcon from '@mui/icons-material/LightModeRounded';
import LockResetRoundedIcon from '@mui/icons-material/LockResetRounded';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import SmartToyRoundedIcon from '@mui/icons-material/SmartToyRounded';
import { PageHeader } from '../../components/common/PageHeader';
import { LoadingState } from '../../components/common/LoadingState';
import { useSaveSettings, useSettings } from '../../hooks/useSettings';
import { useChangePassword } from '../../hooks/usePasswordReset';
import { useTenantSelf, useTenantSelfMessageStats } from '../../hooks/useTenantSelf';
import { useAuth } from '../../hooks/useAuth';
import { useThemeStore } from '../../store/themeStore';
import type { AppSettings } from '../../types/settings';
import type { ApiError } from '../../types/common';
import type { MessageStatsRange } from '../../types/tenant';

const MESSAGE_STATS_RANGE_OPTIONS: { value: MessageStatsRange; label: string }[] = [
  { value: 'ALL_TIME', label: 'All time' },
  { value: 'THIS_MONTH', label: 'This month' },
  { value: 'LAST_MONTH', label: 'Last month' },
];

const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z.string().min(8, 'New password must be at least 8 characters'),
    confirmPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
  });

type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;

export function SettingsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();
  const { data: settings, isLoading } = useSettings();
  const { data: tenantSelf } = useTenantSelf();
  const [messageRange, setMessageRange] = useState<MessageStatsRange>('ALL_TIME');
  const { data: messageStats } = useTenantSelfMessageStats(messageRange);
  const saveSettings = useSaveSettings();
  const changePassword = useChangePassword();
  const { mode, setMode } = useThemeStore();

  const [draft, setDraft] = useState<AppSettings | null>(null);

  const {
    control: passwordControl,
    handleSubmit: handlePasswordSubmit,
    reset: resetPasswordForm,
    formState: { errors: passwordErrors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  });

  const onChangePassword = handlePasswordSubmit((values) => {
    changePassword.mutate(
      { currentPassword: values.currentPassword, newPassword: values.newPassword },
      {
        onSuccess: () => {
          enqueueSnackbar('Password updated', { variant: 'success' });
          resetPasswordForm();
        },
        onError: (error) => {
          enqueueSnackbar((error as ApiError).message, { variant: 'error' });
        },
      },
    );
  });

  useEffect(() => {
    if (settings && !draft) {
      setDraft(settings);
    }
  }, [settings, draft]);

  if (isLoading || !draft) {
    return <LoadingState label="Loading settings..." minHeight={400} />;
  }

  const handleSave = () => {
    saveSettings.mutate(draft, {
      onSuccess: () => enqueueSnackbar('Settings saved', { variant: 'success' }),
    });
  };

  return (
    <Box>
      <PageHeader
        title="Settings"
        subtitle="Configure clinic details, reminders, AI behavior, and appearance."
        actions={
          <Button variant="contained" startIcon={<SaveRoundedIcon />} onClick={handleSave} loading={saveSettings.isPending}>
            Save changes
          </Button>
        }
      />

      <Stack spacing={2.5}>
        <Card>
          <CardHeader title="Clinic Information" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Clinic name"
                  fullWidth
                  value={draft.clinic.clinicName}
                  onChange={(e) => setDraft({ ...draft, clinic: { ...draft.clinic, clinicName: e.target.value } })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Timezone"
                  fullWidth
                  value={draft.clinic.timezone}
                  onChange={(e) => setDraft({ ...draft, clinic: { ...draft.clinic, timezone: e.target.value } })}
                />
              </Grid>
              <Grid size={12}>
                <TextField
                  label="Address"
                  fullWidth
                  value={draft.clinic.address}
                  onChange={(e) => setDraft({ ...draft, clinic: { ...draft.clinic, address: e.target.value } })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Phone number"
                  fullWidth
                  value={draft.clinic.phoneNumber}
                  onChange={(e) => setDraft({ ...draft, clinic: { ...draft.clinic, phoneNumber: e.target.value } })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Email"
                  fullWidth
                  value={draft.clinic.email}
                  onChange={(e) => setDraft({ ...draft, clinic: { ...draft.clinic, email: e.target.value } })}
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {user?.role === 'ADMIN' && tenantSelf && (
          <Card>
            <CardHeader
              title="Clinic Feature Status"
              subheader="Set by your platform provider. Contact them to change a toggle."
              titleTypographyProps={{ variant: 'subtitle1' }}
              action={
                <TextField
                  select
                  size="small"
                  label="Message counts for"
                  value={messageRange}
                  onChange={(e) => setMessageRange(e.target.value as MessageStatsRange)}
                  sx={{ minWidth: 160 }}
                >
                  {MESSAGE_STATS_RANGE_OPTIONS.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>
              }
            />
            <CardContent sx={{ pt: 0 }}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Stack
                    direction="row"
                    spacing={1.5}
                    alignItems="center"
                    justifyContent="space-between"
                    sx={{ p: 2, borderRadius: 2, bgcolor: 'action.hover' }}
                  >
                    <Stack direction="row" spacing={1.5} alignItems="center">
                      <WhatsAppIcon sx={{ color: '#25D366' }} />
                      <Box>
                        <Typography variant="body2" fontWeight={600}>
                          WhatsApp appointment messages
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {messageStats?.whatsappMessageCount ?? tenantSelf.whatsappMessageCount} sent to patients
                        </Typography>
                      </Box>
                    </Stack>
                    <Chip
                      size="small"
                      label={tenantSelf.whatsappNotificationsEnabled ? 'Enabled' : 'Disabled'}
                      color={tenantSelf.whatsappNotificationsEnabled ? 'success' : 'default'}
                    />
                  </Stack>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Stack
                    direction="row"
                    spacing={1.5}
                    alignItems="center"
                    justifyContent="space-between"
                    sx={{ p: 2, borderRadius: 2, bgcolor: 'action.hover' }}
                  >
                    <Stack direction="row" spacing={1.5} alignItems="center">
                      <SmartToyRoundedIcon color="secondary" />
                      <Box>
                        <Typography variant="body2" fontWeight={600}>
                          AI receptionist chat
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {messageStats?.aiChatMessageCount ?? tenantSelf.aiChatMessageCount} messages handled
                        </Typography>
                      </Box>
                    </Stack>
                    <Chip
                      size="small"
                      label={tenantSelf.aiChatEnabled ? 'Enabled' : 'Disabled'}
                      color={tenantSelf.aiChatEnabled ? 'success' : 'default'}
                    />
                  </Stack>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader title="Business Hours" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Stack spacing={1.5}>
              {draft.clinic.businessHours.map((hours, index) => (
                <Stack key={hours.day} direction="row" spacing={2} alignItems="center">
                  <Typography variant="body2" sx={{ width: 48 }} fontWeight={600}>
                    {hours.day}
                  </Typography>
                  <Switch
                    checked={hours.open}
                    onChange={(e) => {
                      const businessHours = [...draft.clinic.businessHours];
                      businessHours[index] = { ...hours, open: e.target.checked };
                      setDraft({ ...draft, clinic: { ...draft.clinic, businessHours } });
                    }}
                  />
                  <TextField
                    type="time"
                    size="small"
                    disabled={!hours.open}
                    value={hours.startTime}
                    onChange={(e) => {
                      const businessHours = [...draft.clinic.businessHours];
                      businessHours[index] = { ...hours, startTime: e.target.value };
                      setDraft({ ...draft, clinic: { ...draft.clinic, businessHours } });
                    }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    to
                  </Typography>
                  <TextField
                    type="time"
                    size="small"
                    disabled={!hours.open}
                    value={hours.endTime}
                    onChange={(e) => {
                      const businessHours = [...draft.clinic.businessHours];
                      businessHours[index] = { ...hours, endTime: e.target.value };
                      setDraft({ ...draft, clinic: { ...draft.clinic, businessHours } });
                    }}
                  />
                </Stack>
              ))}
            </Stack>
          </CardContent>
        </Card>

        <Card>
          <CardHeader title="Reminder Settings" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={draft.reminders.remindersEnabled}
                      onChange={(e) =>
                        setDraft({ ...draft, reminders: { ...draft.reminders, remindersEnabled: e.target.checked } })
                      }
                    />
                  }
                  label="Enable appointment reminders"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  type="number"
                  label="Hours before appointment"
                  fullWidth
                  value={draft.reminders.reminderHoursBefore}
                  onChange={(e) =>
                    setDraft({
                      ...draft,
                      reminders: { ...draft.reminders, reminderHoursBefore: Number(e.target.value) },
                    })
                  }
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={draft.reminders.sendSmsReminders}
                      onChange={(e) =>
                        setDraft({ ...draft, reminders: { ...draft.reminders, sendSmsReminders: e.target.checked } })
                      }
                    />
                  }
                  label="Send SMS reminders"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={draft.reminders.sendEmailReminders}
                      onChange={(e) =>
                        setDraft({ ...draft, reminders: { ...draft.reminders, sendEmailReminders: e.target.checked } })
                      }
                    />
                  }
                  label="Send email reminders"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Escalation email"
                  fullWidth
                  value={draft.reminders.escalationEmail}
                  onChange={(e) =>
                    setDraft({ ...draft, reminders: { ...draft.reminders, escalationEmail: e.target.value } })
                  }
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Escalation phone number"
                  fullWidth
                  value={draft.reminders.escalationPhoneNumber}
                  onChange={(e) =>
                    setDraft({ ...draft, reminders: { ...draft.reminders, escalationPhoneNumber: e.target.value } })
                  }
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        <Card>
          <CardHeader title="AI Configuration" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  select
                  label="Model"
                  fullWidth
                  value={draft.ai.model}
                  onChange={(e) => setDraft({ ...draft, ai: { ...draft.ai, model: e.target.value } })}
                >
                  <MenuItem value="gemini-flash-latest">Gemini Flash (latest)</MenuItem>
                  <MenuItem value="gemini-pro-latest">Gemini Pro (latest)</MenuItem>
                </TextField>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Temperature: {draft.ai.temperature.toFixed(1)}
                </Typography>
                <Slider
                  min={0}
                  max={1}
                  step={0.1}
                  value={draft.ai.temperature}
                  onChange={(_e, value) => setDraft({ ...draft, ai: { ...draft.ai, temperature: value as number } })}
                />
              </Grid>
              <Grid size={12}>
                <TextField
                  label="Greeting message"
                  fullWidth
                  multiline
                  minRows={2}
                  value={draft.ai.greetingMessage}
                  onChange={(e) => setDraft({ ...draft, ai: { ...draft.ai, greetingMessage: e.target.value } })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={draft.ai.autoBookingEnabled}
                      onChange={(e) => setDraft({ ...draft, ai: { ...draft.ai, autoBookingEnabled: e.target.checked } })}
                    />
                  }
                  label="Allow AI to auto-book appointments"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={draft.ai.escalateOnLowConfidence}
                      onChange={(e) =>
                        setDraft({ ...draft, ai: { ...draft.ai, escalateOnLowConfidence: e.target.checked } })
                      }
                    />
                  }
                  label="Escalate to staff on low confidence"
                />
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        <Card>
          <CardHeader title="Theme" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Stack direction="row" spacing={2} alignItems="center">
              <LightModeRoundedIcon color={mode === 'light' ? 'primary' : 'disabled'} />
              <Switch checked={mode === 'dark'} onChange={(e) => setMode(e.target.checked ? 'dark' : 'light')} />
              <DarkModeRoundedIcon color={mode === 'dark' ? 'primary' : 'disabled'} />
              <Typography variant="body2" color="text.secondary">
                {mode === 'dark' ? 'Dark mode' : 'Light mode'}
              </Typography>
            </Stack>
          </CardContent>
        </Card>

        <Card>
          <CardHeader title="Notifications" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Stack spacing={1}>
              <FormControlLabel
                control={
                  <Switch
                    checked={draft.notifications.newAppointmentAlerts}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        notifications: { ...draft.notifications, newAppointmentAlerts: e.target.checked },
                      })
                    }
                  />
                }
                label="New appointment alerts"
              />
              <Divider />
              <FormControlLabel
                control={
                  <Switch
                    checked={draft.notifications.cancellationAlerts}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        notifications: { ...draft.notifications, cancellationAlerts: e.target.checked },
                      })
                    }
                  />
                }
                label="Cancellation alerts"
              />
              <Divider />
              <FormControlLabel
                control={
                  <Switch
                    checked={draft.notifications.dailySummaryEmail}
                    onChange={(e) =>
                      setDraft({
                        ...draft,
                        notifications: { ...draft.notifications, dailySummaryEmail: e.target.checked },
                      })
                    }
                  />
                }
                label="Daily summary email"
              />
            </Stack>
          </CardContent>
        </Card>

        <Card>
          <CardHeader title="Security" titleTypographyProps={{ variant: 'subtitle1' }} />
          <CardContent sx={{ pt: 0 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Change the password for your own account.
            </Typography>
            <Box component="form" onSubmit={onChangePassword} noValidate>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="currentPassword"
                    control={passwordControl}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        type="password"
                        label="Current password"
                        autoComplete="current-password"
                        fullWidth
                        error={Boolean(passwordErrors.currentPassword)}
                        helperText={passwordErrors.currentPassword?.message}
                      />
                    )}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="newPassword"
                    control={passwordControl}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        type="password"
                        label="New password"
                        autoComplete="new-password"
                        fullWidth
                        error={Boolean(passwordErrors.newPassword)}
                        helperText={passwordErrors.newPassword?.message}
                      />
                    )}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="confirmPassword"
                    control={passwordControl}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        type="password"
                        label="Confirm new password"
                        autoComplete="new-password"
                        fullWidth
                        error={Boolean(passwordErrors.confirmPassword)}
                        helperText={passwordErrors.confirmPassword?.message}
                      />
                    )}
                  />
                </Grid>
                <Grid size={12}>
                  <Button
                    type="submit"
                    variant="outlined"
                    startIcon={<LockResetRoundedIcon />}
                    loading={changePassword.isPending}
                  >
                    Update password
                  </Button>
                </Grid>
              </Grid>
            </Box>
          </CardContent>
        </Card>
      </Stack>
    </Box>
  );
}
