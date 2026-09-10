import { useMemo, useState, type MouseEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import {
  Box,
  Button,
  Card,
  Chip,
  IconButton,
  InputAdornment,
  Menu,
  MenuItem,
  TextField,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import MoreVertRoundedIcon from '@mui/icons-material/MoreVertRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { PatientFormDialog } from './PatientFormDialog';
import { useCreatePatient, usePatients, useUpdatePatient } from '../../hooks/usePatients';
import type { Patient, PatientRequest } from '../../types/patient';

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
      headerName: 'Name',
      flex: 1.2,
      minWidth: 180,
      valueGetter: (_value, row) => `${row.firstName} ${row.lastName}`,
    },
    { field: 'phoneNumber', headerName: 'Phone', flex: 1, minWidth: 150 },
    { field: 'email', headerName: 'Email', flex: 1.2, minWidth: 200, valueGetter: (value) => value ?? '—' },
    {
      field: 'dateOfBirth',
      headerName: 'Date of birth',
      flex: 0.8,
      minWidth: 130,
      valueGetter: (value: string | null) => (value ? format(new Date(value), 'MMM d, yyyy') : '—'),
    },
    {
      field: 'gender',
      headerName: 'Gender',
      flex: 0.6,
      minWidth: 110,
      renderCell: (params) =>
        params.value ? <Chip size="small" label={params.value} variant="outlined" /> : '—',
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
      <PageHeader
        title="Patients"
        subtitle="View, search, and manage patient records."
        actions={
          <Button
            variant="contained"
            startIcon={<AddRoundedIcon />}
            onClick={() => {
              setEditingPatient(null);
              setFormOpen(true);
            }}
          >
            Create Patient
          </Button>
        }
      />

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
            sx={{ border: 'none' }}
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
