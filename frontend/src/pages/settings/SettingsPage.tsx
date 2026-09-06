import { useEffect, useState } from 'react';
import { useSnackbar } from 'notistack';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
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
import { PageHeader } from '../../components/common/PageHeader';
import { LoadingState } from '../../components/common/LoadingState';
import { useSaveSettings, useSettings } from '../../hooks/useSettings';
import { useThemeStore } from '../../store/themeStore';
import type { AppSettings } from '../../types/settings';

export function SettingsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { data: settings, isLoading } = useSettings();
  const saveSettings = useSaveSettings();
  const { mode, setMode } = useThemeStore();

  const [draft, setDraft] = useState<AppSettings | null>(null);

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
      </Stack>
    </Box>
  );
}
