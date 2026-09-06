import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Chip,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { LoadingState } from '../../components/common/LoadingState';
import { EmptyState } from '../../components/common/EmptyState';
import { StatusChip } from '../../components/common/StatusChip';
import { useAddPatientNote, usePatient, usePatientNotes } from '../../hooks/usePatients';
import { useAppointments } from '../../hooks/useAppointments';
import { useAuthStore } from '../../store/authStore';
import { useQueryClient } from '@tanstack/react-query';
import { patientService } from '../../services/patientService';
import { queryKeys } from '../../api/queryKeys';

export function PatientDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const queryClient = useQueryClient();
  const currentUser = useAuthStore((s) => s.user);

  const { data: patient, isLoading } = usePatient(id);
  const { data: notes = [] } = usePatientNotes(id);
  const { data: appointments = [] } = useAppointments({ patientId: id });
  const addNote = useAddPatientNote();

  const [noteText, setNoteText] = useState('');

  if (isLoading) {
    return <LoadingState label="Loading patient..." minHeight={400} />;
  }

  if (!patient) {
    return (
      <EmptyState
        title="Patient not found"
        description="This patient may have been removed."
        action={
          <Button variant="contained" onClick={() => navigate('/patients')}>
            Back to Patients
          </Button>
        }
      />
    );
  }

  const submitNote = () => {
    if (!noteText.trim() || !id) return;
    addNote.mutate(
      { patientId: id, note: noteText.trim(), createdBy: currentUser?.email ?? 'Staff' },
      {
        onSuccess: () => {
          setNoteText('');
          enqueueSnackbar('Note added', { variant: 'success' });
        },
      },
    );
  };

  const removeNote = (noteId: string) => {
    patientService.removeNote(noteId);
    queryClient.invalidateQueries({ queryKey: queryKeys.patientNotes(id ?? '') });
  };

  return (
    <Box>
      <PageHeader
        title={`${patient.firstName} ${patient.lastName}`}
        subtitle="Patient profile, appointment history, and medical notes."
        actions={
          <Button startIcon={<ArrowBackRoundedIcon />} onClick={() => navigate('/patients')}>
            Back to Patients
          </Button>
        }
      />

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Stack alignItems="center" spacing={1.5} sx={{ mb: 2 }}>
                <Avatar sx={{ width: 72, height: 72, bgcolor: 'primary.main', fontSize: 28 }}>
                  {patient.firstName.charAt(0)}
                  {patient.lastName.charAt(0)}
                </Avatar>
                <Typography variant="h6">
                  {patient.firstName} {patient.lastName}
                </Typography>
                {patient.gender && <Chip size="small" label={patient.gender} variant="outlined" />}
              </Stack>
              <Divider sx={{ mb: 2 }} />
              <Stack spacing={1.5}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Phone
                  </Typography>
                  <Typography variant="body2">{patient.phoneNumber}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Email
                  </Typography>
                  <Typography variant="body2">{patient.email ?? '—'}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Date of birth
                  </Typography>
                  <Typography variant="body2">
                    {patient.dateOfBirth ? format(new Date(patient.dateOfBirth), 'MMM d, yyyy') : '—'}
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 8 }}>
          <Stack spacing={2.5}>
            <Card>
              <CardHeader title="Appointment history" titleTypographyProps={{ variant: 'subtitle1' }} />
              <CardContent sx={{ pt: 0 }}>
                {appointments.length === 0 ? (
                  <EmptyState title="No appointments yet" />
                ) : (
                  <List disablePadding>
                    {appointments.map((appt) => (
                      <ListItem key={appt.id} divider disableGutters>
                        <ListItemText
                          primary={`${appt.doctorName} — ${appt.reason ?? 'General visit'}`}
                          secondary={format(new Date(appt.scheduledStart), 'MMM d, yyyy h:mm a')}
                        />
                        <StatusChip status={appt.status} />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader title="Medical notes" titleTypographyProps={{ variant: 'subtitle1' }} />
              <CardContent sx={{ pt: 0 }}>
                <Stack direction="row" spacing={1.5} sx={{ mb: 2 }}>
                  <TextField
                    fullWidth
                    size="small"
                    placeholder="Add a medical note..."
                    value={noteText}
                    onChange={(e) => setNoteText(e.target.value)}
                    multiline
                    minRows={1}
                    maxRows={4}
                  />
                  <Button variant="contained" onClick={submitNote} loading={addNote.isPending} sx={{ flexShrink: 0 }}>
                    Add
                  </Button>
                </Stack>
                {notes.length === 0 ? (
                  <EmptyState title="No medical notes recorded" />
                ) : (
                  <List disablePadding>
                    {notes.map((note) => (
                      <ListItem
                        key={note.id}
                        divider
                        disableGutters
                        secondaryAction={
                          <IconButton edge="end" size="small" onClick={() => removeNote(note.id)}>
                            <DeleteRoundedIcon fontSize="small" />
                          </IconButton>
                        }
                      >
                        <ListItemText
                          primary={note.note}
                          secondary={`${note.createdBy} · ${format(new Date(note.createdAt), 'MMM d, yyyy h:mm a')}`}
                        />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>
    </Box>
  );
}
