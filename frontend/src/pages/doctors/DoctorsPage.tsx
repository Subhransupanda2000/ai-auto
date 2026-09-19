import { useMemo, useState } from 'react';
import { useSnackbar } from 'notistack';
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  IconButton,
  InputAdornment,
  Menu,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import ScheduleRoundedIcon from '@mui/icons-material/ScheduleRounded';
import MedicalServicesRoundedIcon from '@mui/icons-material/MedicalServicesRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import CategoryRoundedIcon from '@mui/icons-material/CategoryRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import CallRoundedIcon from '@mui/icons-material/CallRounded';
import { PageHero } from '../../components/common/PageHero';
import { StatCard } from '../../components/common/StatCard';
import { EmptyState } from '../../components/common/EmptyState';
import { LoadingState } from '../../components/common/LoadingState';
import { ConfirmDialog } from '../../components/common/ConfirmDialog';
import { DoctorFormDialog } from './DoctorFormDialog';
import { useCreateDoctor, useDeleteDoctor, useDoctors, useUpdateDoctor } from '../../hooks/useDoctors';
import type { DoctorRequest, DoctorWithSchedule } from '../../types/doctor';

const AVATAR_GRADIENTS = [
  'linear-gradient(135deg, #3B6FE0, #6C93EA)',
  'linear-gradient(135deg, #0FB5A7, #4CCBC0)',
  'linear-gradient(135deg, #E6A23C, #F0C778)',
  'linear-gradient(135deg, #8E6FE0, #B79CF0)',
  'linear-gradient(135deg, #E4574C, #F0938A)',
];

function gradientFor(id: string) {
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) hash = id.charCodeAt(i) + ((hash << 5) - hash);
  return AVATAR_GRADIENTS[Math.abs(hash) % AVATAR_GRADIENTS.length];
}

export function DoctorsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { data: doctors = [], isLoading } = useDoctors();
  const createDoctor = useCreateDoctor();
  const updateDoctor = useUpdateDoctor();
  const deleteDoctor = useDeleteDoctor();

  const [search, setSearch] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editingDoctor, setEditingDoctor] = useState<DoctorWithSchedule | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DoctorWithSchedule | null>(null);
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const [menuDoctor, setMenuDoctor] = useState<DoctorWithSchedule | null>(null);

  const filteredDoctors = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return doctors;
    return doctors.filter((d) =>
      `${d.firstName} ${d.lastName} ${d.specialty}`.toLowerCase().includes(q),
    );
  }, [doctors, search]);

  const stats = useMemo(() => {
    const active = doctors.filter((d) => d.active).length;
    const specialties = new Set(doctors.map((d) => d.specialty)).size;
    return { total: doctors.length, active, specialties };
  }, [doctors]);

  const handleCreate = (values: DoctorRequest) => {
    createDoctor.mutate(values, {
      onSuccess: () => {
        enqueueSnackbar('Doctor created', { variant: 'success' });
        setFormOpen(false);
      },
    });
  };

  const handleUpdate = (values: DoctorRequest) => {
    if (!editingDoctor) return;
    updateDoctor.mutate(
      { id: editingDoctor.id, payload: values },
      {
        onSuccess: () => {
          enqueueSnackbar('Doctor updated', { variant: 'success' });
          setFormOpen(false);
          setEditingDoctor(null);
        },
      },
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    deleteDoctor.mutate(deleteTarget.id, {
      onSuccess: () => {
        enqueueSnackbar('Doctor deleted', { variant: 'success' });
        setDeleteTarget(null);
      },
    });
  };

  return (
    <Box>
      <PageHero
        title="Doctors"
        subtitle="Manage clinicians, specialties, and availability."
        icon={<MedicalServicesRoundedIcon />}
        gradient="linear-gradient(135deg, #0FB5A7 0%, #3B6FE0 65%, #2A50A8 130%)"
        actions={
          <Button
            variant="contained"
            startIcon={<AddRoundedIcon />}
            onClick={() => {
              setEditingDoctor(null);
              setFormOpen(true);
            }}
            sx={{
              bgcolor: '#FFFFFF',
              color: 'primary.dark',
              fontWeight: 700,
              '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' },
            }}
          >
            Add Doctor
          </Button>
        }
      />

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <StatCard label="Total Doctors" value={stats.total} icon={<MedicalServicesRoundedIcon />} color="primary" />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <StatCard
            label="Active"
            value={`${stats.active}/${stats.total}`}
            icon={<CheckCircleRoundedIcon />}
            color="success"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <StatCard label="Specialties Covered" value={stats.specialties} icon={<CategoryRoundedIcon />} color="secondary" />
        </Grid>
      </Grid>

      <TextField
        placeholder="Search by name or specialty..."
        size="small"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        sx={{ maxWidth: 420, mb: 2.5 }}
        fullWidth
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchRoundedIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
          },
        }}
      />

      {isLoading ? (
        <LoadingState label="Loading doctors..." />
      ) : filteredDoctors.length === 0 ? (
        <EmptyState title="No doctors found" description="Try adjusting your search or add a new doctor." />
      ) : (
        <Grid container spacing={2.5}>
          {filteredDoctors.map((doctor) => (
            <Grid key={doctor.id} size={{ xs: 12, sm: 6, lg: 4 }}>
              <Card
                sx={{
                  height: '100%',
                  transition: 'transform 0.18s ease, box-shadow 0.18s ease',
                  '&:hover': {
                    transform: 'translateY(-3px)',
                    boxShadow: '0 12px 28px rgba(15, 23, 42, 0.12)',
                  },
                }}
              >
                <Box sx={{ height: 6, background: gradientFor(doctor.id) }} />
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                    <Stack direction="row" spacing={1.5} alignItems="center">
                      <Avatar
                        sx={{
                          width: 52,
                          height: 52,
                          fontWeight: 700,
                          background: gradientFor(doctor.id),
                        }}
                      >
                        {doctor.firstName.charAt(0)}
                        {doctor.lastName.charAt(0)}
                      </Avatar>
                      <Box>
                        <Typography variant="subtitle1" lineHeight={1.2}>
                          Dr. {doctor.firstName} {doctor.lastName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {doctor.specialty}
                        </Typography>
                      </Box>
                    </Stack>
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        setMenuAnchor(e.currentTarget);
                        setMenuDoctor(doctor);
                      }}
                    >
                      <MoreVertRoundedIcon fontSize="small" />
                    </IconButton>
                  </Stack>

                  <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
                    <Chip
                      size="small"
                      label={doctor.active ? 'Active' : 'Inactive'}
                      color={doctor.active ? 'success' : 'default'}
                    />
                  </Stack>

                  <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 2 }}>
                    <ScheduleRoundedIcon fontSize="small" color="action" />
                    <Typography variant="body2" color="text.secondary">
                      {doctor.workingHoursStart}–{doctor.workingHoursEnd} · {doctor.workingDays.join(', ')}
                    </Typography>
                  </Stack>

                  {doctor.bio && (
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1.5 }}>
                      {doctor.bio}
                    </Typography>
                  )}

                  <Divider sx={{ my: 2 }} />

                  <Stack spacing={1}>
                    <Stack direction="row" spacing={1} alignItems="center">
                      <EmailRoundedIcon sx={{ fontSize: 16 }} color="disabled" />
                      <Typography variant="caption" color="text.secondary">
                        {doctor.email}
                      </Typography>
                    </Stack>
                    <Stack direction="row" spacing={1} alignItems="center">
                      <CallRoundedIcon sx={{ fontSize: 16 }} color="disabled" />
                      <Typography variant="caption" color="text.secondary">
                        {doctor.phoneNumber}
                      </Typography>
                    </Stack>
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
        <MenuItem
          onClick={() => {
            setEditingDoctor(menuDoctor);
            setFormOpen(true);
            setMenuAnchor(null);
          }}
        >
          <EditRoundedIcon fontSize="small" sx={{ mr: 1.5 }} /> Edit
        </MenuItem>
        <MenuItem
          onClick={() => {
            setDeleteTarget(menuDoctor);
            setMenuAnchor(null);
          }}
          sx={{ color: 'error.main' }}
        >
          <DeleteRoundedIcon fontSize="small" sx={{ mr: 1.5 }} /> Delete
        </MenuItem>
      </Menu>

      <DoctorFormDialog
        open={formOpen}
        doctor={editingDoctor}
        loading={createDoctor.isPending || updateDoctor.isPending}
        onClose={() => {
          setFormOpen(false);
          setEditingDoctor(null);
        }}
        onSubmit={editingDoctor ? handleUpdate : handleCreate}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete doctor"
        message={`Are you sure you want to remove Dr. ${deleteTarget?.firstName} ${deleteTarget?.lastName}?`}
        confirmLabel="Delete"
        destructive
        loading={deleteDoctor.isPending}
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
      />
    </Box>
  );
}
