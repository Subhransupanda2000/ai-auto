import { useState } from 'react';
import { useSnackbar } from 'notistack';
import { Button, Card, Chip, Switch, Tooltip } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { CreateTenantDialog } from './CreateTenantDialog';
import { useCreateTenant, useTenants, useUpdateTenantStatus } from '../../hooks/useTenants';
import type { CreateTenantRequest, Tenant } from '../../types/tenant';

export function TenantsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { data: tenants = [], isLoading } = useTenants();
  const createTenant = useCreateTenant();
  const updateStatus = useUpdateTenantStatus();

  const [formOpen, setFormOpen] = useState(false);

  const handleCreate = (values: CreateTenantRequest) => {
    createTenant.mutate(values, {
      onSuccess: (tenant) => {
        enqueueSnackbar(`Clinic "${tenant.name}" created`, { variant: 'success' });
        setFormOpen(false);
      },
      onError: (error) => {
        enqueueSnackbar(error.message, { variant: 'error' });
      },
    });
  };

  const handleToggleActive = (tenant: Tenant) => {
    updateStatus.mutate(
      { id: tenant.id, active: !tenant.active },
      {
        onSuccess: (updated) => {
          enqueueSnackbar(`"${updated.name}" ${updated.active ? 'activated' : 'deactivated'}`, { variant: 'success' });
        },
        onError: (error) => {
          enqueueSnackbar(error.message, { variant: 'error' });
        },
      },
    );
  };

  const columns: GridColDef<Tenant>[] = [
    { field: 'name', headerName: 'Clinic', flex: 1.2, minWidth: 200 },
    { field: 'slug', headerName: 'Slug', flex: 1, minWidth: 160 },
    {
      field: 'userCount',
      headerName: 'Staff',
      flex: 0.5,
      minWidth: 90,
      renderCell: (params) => <Chip size="small" label={params.value} variant="outlined" />,
    },
    {
      field: 'createdAt',
      headerName: 'Onboarded',
      flex: 0.8,
      minWidth: 150,
      valueGetter: (value: string) => (value ? format(new Date(value), 'MMM d, yyyy') : '—'),
    },
    {
      field: 'active',
      headerName: 'Active',
      flex: 0.6,
      minWidth: 110,
      sortable: false,
      renderCell: (params) => (
        <Tooltip title={params.row.active ? 'Deactivate this clinic' : 'Activate this clinic'}>
          <Switch
            checked={params.row.active}
            size="small"
            onChange={() => handleToggleActive(params.row)}
            disabled={updateStatus.isPending}
          />
        </Tooltip>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Tenants"
        subtitle="Onboard and manage clinic tenants. Each clinic's staff, patients, doctors, and appointments are fully isolated from every other clinic."
        actions={
          <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={() => setFormOpen(true)}>
            New Tenant
          </Button>
        }
      />

      <Card>
        <DataGrid
          rows={tenants}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        />
      </Card>

      <CreateTenantDialog
        open={formOpen}
        loading={createTenant.isPending}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreate}
      />
    </>
  );
}
