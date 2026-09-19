import { useMemo, useState, type MouseEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import {
  Avatar,
  Box,
  Button,
  Card,
  Chip,
  IconButton,
  InputAdornment,
  Menu,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import WcRoundedIcon from '@mui/icons-material/WcRounded';
import CakeRoundedIcon from '@mui/icons-material/CakeRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import { differenceInYears, isThisMonth } from 'date-fns';
import { PageHero } from '../../components/common/PageHero';
import { StatCard } from '../../components/common/StatCard';
import { PatientFormDialog } from './PatientFormDialog';
import { useCreatePatient, usePatients, useUpdatePatient } from '../../hooks/usePatients';
import type { Patient, PatientRequest } from '../../types/patient';

const AVATAR_PALETTE = ['#3B6FE0', '#0FB5A7', '#E6A23C', '#E4574C', '#8E6FE0', '#3B9FE0'];

function avatarColorFor(id: string) {
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) hash = id.charCodeAt(i) + ((hash << 5) - hash);
  return AVATAR_PALETTE[Math.abs(hash) % AVATAR_PALETTE.length];
}

export function PatientsPage() {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const { data: patients = [], isLoading } = usePatients();
  const createPatient = useCreatePatient();
  const updatePatient = useUpdatePatient();

  const [search, setSearch] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null);
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const [menuPatient, setMenuPatient] = useState<Patient | null>(null);

  const filteredPatients = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return patients;
    return patients.filter((p) =>
      `${p.firstName} ${p.lastName} ${p.phoneNumber} ${p.email ?? ''}`.toLowerCase().includes(q),
    );
  }, [patients, search]);

  const stats = useMemo(() => {
    const withEmail = patients.filter((p) => Boolean(p.email)).length;
    const female = patients.filter((p) => p.gender === 'FEMALE').length;
    const male = patients.filter((p) => p.gender === 'MALE').length;
    const registeredThisMonth = patients.filter(
      (p) => p.dateOfBirth && isThisMonth(new Date(p.dateOfBirth)),
    ).length;
    return { total: patients.length, withEmail, female, male, registeredThisMonth };
  }, [patients]);

  const openMenu = (e: MouseEvent<HTMLElement>, patient: Patient) => {
    setMenuAnchor(e.currentTarget);
    setMenuPatient(patient);
  };
  const closeMenu = () => {
    setMenuAnchor(null);
    setMenuPatient(null);
  };

  const columns: GridColDef<Patient>[] = [
    {
      field: 'name',
      headerName: 'Patient',
      flex: 1.4,
      minWidth: 220,
      valueGetter: (_value, row) => `${row.firstName} ${row.lastName}`,
      renderCell: (params) => (
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ py: 1.25 }}>
          <Avatar
            sx={{
              width: 34,
              height: 34,
              fontSize: 13,
              fontWeight: 700,
              flexShrink: 0,
              bgcolor: avatarColorFor(params.row.id),
            }}
          >
            {params.row.firstName.charAt(0)}
            {params.row.lastName.charAt(0)}
          </Avatar>
          <Box sx={{ minWidth: 0, lineHeight: 1.35 }}>
            <Typography variant="body2" fontWeight={600} noWrap>
              {params.row.firstName} {params.row.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary" noWrap component="div">
              {params.row.phoneNumber}
            </Typography>
          </Box>
        </Stack>
      ),
    },
    { field: 'email', headerName: 'Email', flex: 1.2, minWidth: 200, valueGetter: (value) => value ?? '—' },
    {
      field: 'dateOfBirth',
      headerName: 'Age',
      flex: 0.6,
      minWidth: 100,
      valueGetter: (value: string | null) => (value ? `${differenceInYears(new Date(), new Date(value))} yrs` : '—'),
    },
    {
      field: 'gender',
      headerName: 'Gender',
      flex: 0.6,
      minWidth: 110,
      renderCell: (params) =>
        params.value ? (
          <Chip
            size="small"
            label={params.value}
            variant="outlined"
            color={params.value === 'FEMALE' ? 'secondary' : params.value === 'MALE' ? 'primary' : 'default'}
          />
        ) : (
          '—'
        ),
    },
    {
      field: 'actions',
      headerName: '',
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      width: 60,
      renderCell: (params) => (
        <IconButton size="small" onClick={(e) => openMenu(e, params.row)}>
          <MoreVertRoundedIcon fontSize="small" />
        </IconButton>
      ),
    },
  ];

  const handleCreate = (values: PatientRequest) => {
    createPatient.mutate(values, {
      onSuccess: () => {
        enqueueSnackbar('Patient created', { variant: 'success' });
        setFormOpen(false);
      },
    });
  };

  const handleUpdate = (values: PatientRequest) => {
    if (!editingPatient) return;
    updatePatient.mutate(
      { id: editingPatient.id, payload: values },
      {
        onSuccess: () => {
          enqueueSnackbar('Patient updated', { variant: 'success' });
          setFormOpen(false);
          setEditingPatient(null);
        },
      },
    );
  };

  return (
    <Box>
      <PageHero
        title="Patients"
        subtitle="View, search, and manage patient records across your clinic."
        icon={<PeopleAltRoundedIcon />}
        actions={
          <Button
            variant="contained"
            startIcon={<AddRoundedIcon />}
            onClick={() => {
              setEditingPatient(null);
              setFormOpen(true);
            }}
            sx={{
              bgcolor: '#FFFFFF',
              color: 'primary.dark',
              fontWeight: 700,
              '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' },
            }}
          >
            Create Patient
          </Button>
        }
      />

      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="Total Patients" value={stats.total} icon={<PeopleAltRoundedIcon />} color="primary" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Birthdays This Month"
            value={stats.registeredThisMonth}
            icon={<CakeRoundedIcon />}
            color="secondary"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard label="With Email on File" value={stats.withEmail} icon={<EmailRoundedIcon />} color="info" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            label="Gender Mix"
            value={`${stats.female} / ${stats.male}`}
            icon={<WcRoundedIcon />}
            color="warning"
            helperText="female / male"
          />
        </Grid>
      </Grid>

      <Card>
        <Box sx={{ p: 2 }}>
          <TextField
            placeholder="Search patients by name, phone, or email..."
            size="small"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            sx={{ maxWidth: 420 }}
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
        </Box>
        <Box sx={{ height: 600 }}>
          <DataGrid
            rows={filteredPatients}
            columns={columns}
            loading={isLoading}
            disableRowSelectionOnClick
            pageSizeOptions={[10, 25, 50]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            onRowDoubleClick={(params) => navigate(`/patients/${params.id}`)}
            getRowHeight={() => 'auto'}
            sx={{
              border: 'none',
              '--DataGrid-rowBorderColor': 'transparent',
              '& .MuiDataGrid-columnHeaders': {
                bgcolor: 'action.hover',
                borderRadius: 0,
              },
              '& .MuiDataGrid-cell': { display: 'flex', alignItems: 'center' },
              '& .MuiDataGrid-row': { cursor: 'pointer' },
              '& .MuiDataGrid-row:hover': { bgcolor: 'action.hover' },
            }}
          />
        </Box>
      </Card>

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={closeMenu}>
        <MenuItem
          onClick={() => {
            if (menuPatient) navigate(`/patients/${menuPatient.id}`);
            closeMenu();
          }}
        >
          <VisibilityRoundedIcon fontSize="small" sx={{ mr: 1.5 }} /> View profile
        </MenuItem>
        <MenuItem
          onClick={() => {
            setEditingPatient(menuPatient);
            setFormOpen(true);
            closeMenu();
          }}
        >
          <EditRoundedIcon fontSize="small" sx={{ mr: 1.5 }} /> Edit
        </MenuItem>
      </Menu>

      <PatientFormDialog
        open={formOpen}
        patient={editingPatient}
        loading={createPatient.isPending || updatePatient.isPending}
        onClose={() => {
          setFormOpen(false);
          setEditingPatient(null);
        }}
        onSubmit={editingPatient ? handleUpdate : handleCreate}
      />
    </Box>
  );
}
