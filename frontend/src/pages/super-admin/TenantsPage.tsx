import { useState } from 'react';
import { useSnackbar } from 'notistack';
import { Button, Card, Chip, IconButton, MenuItem, Stack, Switch, TextField, Tooltip, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import SmartToyRoundedIcon from '@mui/icons-material/SmartToyRounded';
import LockResetRoundedIcon from '@mui/icons-material/LockResetRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { PasswordRevealDialog } from '../../components/common/PasswordRevealDialog';
import { CreateTenantDialog } from './CreateTenantDialog';
import {
  useCreateTenant,
  useResetTenantAdminPassword,
  useTenants,
  useUpdateTenantAiChat,
  useUpdateTenantStatus,
  useUpdateTenantWhatsapp,
} from '../../hooks/useTenants';
import type { CreateTenantRequest, MessageStatsRange, Tenant, TenantAdminPasswordReset } from '../../types/tenant';

const MESSAGE_STATS_RANGE_OPTIONS: { value: MessageStatsRange; label: string }[] = [
  { value: 'ALL_TIME', label: 'All time' },
  { value: 'THIS_MONTH', label: 'This month' },
  { value: 'LAST_MONTH', label: 'Last month' },
];

export function TenantsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const [messageRange, setMessageRange] = useState<MessageStatsRange>('ALL_TIME');
  const { data: tenants = [], isLoading } = useTenants(messageRange);
  const createTenant = useCreateTenant();
  const updateStatus = useUpdateTenantStatus();
  const updateWhatsapp = useUpdateTenantWhatsapp();
  const updateAiChat = useUpdateTenantAiChat();
  const resetAdminPassword = useResetTenantAdminPassword();

  const [formOpen, setFormOpen] = useState(false);
  const [resetTarget, setResetTarget] = useState<Tenant | null>(null);
  const [resetResult, setResetResult] = useState<TenantAdminPasswordReset | null>(null);

  const rangeLabel = MESSAGE_STATS_RANGE_OPTIONS.find((o) => o.value === messageRange)?.label ?? '';

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

  const handleToggleWhatsapp = (tenant: Tenant) => {
    updateWhatsapp.mutate(
      { id: tenant.id, enabled: !tenant.whatsappNotificationsEnabled },
      {
        onSuccess: (updated) => {
          enqueueSnackbar(
            `WhatsApp appointment messages ${updated.whatsappNotificationsEnabled ? 'enabled' : 'disabled'} for "${updated.name}"`,
            { variant: 'success' },
          );
        },
        onError: (error) => {
          enqueueSnackbar(error.message, { variant: 'error' });
        },
      },
    );
  };

  const handleToggleAiChat = (tenant: Tenant) => {
    updateAiChat.mutate(
      { id: tenant.id, enabled: !tenant.aiChatEnabled },
      {
        onSuccess: (updated) => {
          enqueueSnackbar(
            `AI chat assistant ${updated.aiChatEnabled ? 'enabled' : 'disabled'} for "${updated.name}"`,
            { variant: 'success' },
          );
        },
        onError: (error) => {
          enqueueSnackbar(error.message, { variant: 'error' });
        },
      },
    );
  };

  const handleResetPassword = (tenant: Tenant) => {
    setResetTarget(tenant);
    setResetResult(null);
    resetAdminPassword.mutate(tenant.id, {
      onSuccess: (result) => setResetResult(result),
      onError: (error) => {
        enqueueSnackbar(error.message, { variant: 'error' });
        setResetTarget(null);
      },
    });
  };

  const columns: GridColDef<Tenant>[] = [
    { field: 'name', headerName: 'Clinic', flex: 1.1, minWidth: 180 },
    { field: 'slug', headerName: 'Slug', flex: 0.9, minWidth: 140 },
    {
      field: 'userCount',
      headerName: 'Staff',
      flex: 0.4,
      minWidth: 80,
      renderCell: (params) => <Chip size="small" label={params.value} variant="outlined" />,
    },
    {
      field: 'createdAt',
      headerName: 'Onboarded',
      flex: 0.7,
      minWidth: 130,
      valueGetter: (value: string) => (value ? format(new Date(value), 'MMM d, yyyy') : '—'),
    },
    {
      field: 'active',
      headerName: 'Active',
      flex: 0.45,
      minWidth: 90,
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
    {
      field: 'whatsappNotificationsEnabled',
      headerName: 'WhatsApp',
      flex: 0.55,
      minWidth: 110,
      sortable: false,
      renderCell: (params) => (
        <Tooltip
          title={
            params.row.whatsappNotificationsEnabled
              ? 'Disable WhatsApp messages for scheduled/rescheduled/cancelled/completed appointments'
              : 'Enable WhatsApp messages for scheduled/rescheduled/cancelled/completed appointments'
          }
        >
          <span>
            <Switch
              checked={params.row.whatsappNotificationsEnabled}
              size="small"
              color="success"
              onChange={() => handleToggleWhatsapp(params.row)}
              disabled={updateWhatsapp.isPending}
            />
          </span>
        </Tooltip>
      ),
    },
    {
      field: 'whatsappMessageCount',
      headerName: `WhatsApp Sent (${rangeLabel})`,
      flex: 0.75,
      minWidth: 160,
      renderCell: (params) => (
        <Stack direction="row" spacing={0.75} alignItems="center" sx={{ height: '100%' }}>
          <WhatsAppIcon fontSize="small" sx={{ color: '#25D366' }} />
          <Typography variant="body2" fontWeight={600}>
            {params.value}
          </Typography>
        </Stack>
      ),
    },
    {
      field: 'aiChatEnabled',
      headerName: 'AI Chat',
      flex: 0.55,
      minWidth: 100,
      sortable: false,
      renderCell: (params) => (
        <Tooltip
          title={
            params.row.aiChatEnabled
              ? 'Disable the AI receptionist chat for this clinic'
              : 'Enable the AI receptionist chat for this clinic'
          }
        >
          <span>
            <Switch
              checked={params.row.aiChatEnabled}
              size="small"
              color="secondary"
              onChange={() => handleToggleAiChat(params.row)}
              disabled={updateAiChat.isPending}
            />
          </span>
        </Tooltip>
      ),
    },
    {
      field: 'aiChatMessageCount',
      headerName: `AI Messages (${rangeLabel})`,
      flex: 0.75,
      minWidth: 160,
      renderCell: (params) => (
        <Stack direction="row" spacing={0.75} alignItems="center" sx={{ height: '100%' }}>
          <SmartToyRoundedIcon fontSize="small" color="secondary" />
          <Typography variant="body2" fontWeight={600}>
            {params.value}
          </Typography>
        </Stack>
      ),
    },
    {
      field: 'actions',
      headerName: '',
      flex: 0.4,
      minWidth: 70,
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      renderCell: (params) => (
        <Tooltip title="Reset this clinic's admin password">
          <IconButton size="small" onClick={() => handleResetPassword(params.row)}>
            <LockResetRoundedIcon fontSize="small" />
          </IconButton>
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
        <Stack
          direction="row"
          spacing={2}
          alignItems="center"
          justifyContent="flex-end"
          sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}
        >
          <Typography variant="body2" color="text.secondary">
            WhatsApp / AI message counts for:
          </Typography>
          <TextField
            select
            size="small"
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
        </Stack>
        <DataGrid
          rows={tenants}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          autoHeight
          getRowHeight={() => 'auto'}
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          sx={{ '& .MuiDataGrid-cell': { display: 'flex', alignItems: 'center', py: 1 } }}
        />
      </Card>

      <CreateTenantDialog
        open={formOpen}
        loading={createTenant.isPending}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreate}
      />

      <PasswordRevealDialog
        open={Boolean(resetTarget)}
        title={`New temporary password${resetTarget ? ` for ${resetTarget.name}` : ''}`}
        result={resetResult ? { email: resetResult.adminEmail, temporaryPassword: resetResult.temporaryPassword } : null}
        onClose={() => {
          setResetTarget(null);
          setResetResult(null);
        }}
      />
    </>
  );
}
