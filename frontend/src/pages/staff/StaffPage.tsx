import { useMemo, useState } from 'react';
import { useSnackbar } from 'notistack';
import {
  Box,
  Button,
  Card,
  Chip,
  IconButton,
  InputAdornment,
  Stack,
  Switch,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import LockResetRoundedIcon from '@mui/icons-material/LockResetRounded';
import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { PasswordRevealDialog } from '../../components/common/PasswordRevealDialog';
import { EmptyState } from '../../components/common/EmptyState';
import { InviteStaffDialog } from './InviteStaffDialog';
import { useAuth } from '../../hooks/useAuth';
import { useInviteStaff, useResetStaffPassword, useStaff, useUpdateStaffStatus } from '../../hooks/useStaff';
import type { RegisterRequest, UserResponse } from '../../types/auth';
import type { UserPasswordReset } from '../../api/userApi';

const ROLE_COLOR: Record<string, 'primary' | 'secondary' | 'default'> = {
  ADMIN: 'primary',
  DOCTOR: 'secondary',
  RECEPTIONIST: 'default',
};

export function StaffPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { user: currentUser } = useAuth();
  const isAdmin = currentUser?.role === 'ADMIN';
  const { data: staff = [], isLoading } = useStaff(isAdmin);
  const inviteStaff = useInviteStaff();
  const updateStatus = useUpdateStaffStatus();
  const resetPassword = useResetStaffPassword();

  const [search, setSearch] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [resetTarget, setResetTarget] = useState<UserResponse | null>(null);
  const [resetResult, setResetResult] = useState<UserPasswordReset | null>(null);

  const filteredStaff = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return staff;
    return staff.filter((s) => `${s.fullName} ${s.email} ${s.role}`.toLowerCase().includes(q));
  }, [staff, search]);

  const handleInvite = (values: RegisterRequest) => {
    inviteStaff.mutate(values, {
      onSuccess: (created) => {
        enqueueSnackbar(`Invited ${created.fullName} as ${created.role}`, { variant: 'success' });
        setFormOpen(false);
      },
      onError: (error) => enqueueSnackbar(error.message, { variant: 'error' }),
    });
  };

  const handleToggleStatus = (member: UserResponse) => {
    updateStatus.mutate(
      { id: member.id, enabled: !member.enabled },
      {
        onSuccess: (updated) => {
          enqueueSnackbar(`${updated.fullName} ${updated.enabled ? 'activated' : 'deactivated'}`, { variant: 'success' });
        },
        onError: (error) => enqueueSnackbar(error.message, { variant: 'error' }),
      },
    );
  };

  const handleResetPassword = (member: UserResponse) => {
    setResetTarget(member);
    setResetResult(null);
    resetPassword.mutate(member.id, {
      onSuccess: (result) => setResetResult(result),
      onError: (error) => {
        enqueueSnackbar(error.message, { variant: 'error' });
        setResetTarget(null);
      },
    });
  };

  const columns: GridColDef<UserResponse>[] = [
    {
      field: 'fullName',
      headerName: 'Name',
      flex: 1.2,
      minWidth: 190,
      renderCell: (params) => (
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ height: '100%' }}>
          <Box
            sx={{
              width: 34,
              height: 34,
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 13,
              fontWeight: 700,
              color: '#fff',
              bgcolor: 'primary.main',
              flexShrink: 0,
            }}
          >
            {params.row.fullName.slice(0, 2).toUpperCase()}
          </Box>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="body2" fontWeight={600} noWrap>
              {params.row.fullName}
              {params.row.email.toLowerCase() === currentUser?.email?.toLowerCase() && (
                <Typography component="span" variant="caption" color="text.secondary">
                  {' '}
                  (you)
                </Typography>
              )}
            </Typography>
            <Typography variant="caption" color="text.secondary" noWrap component="div">
              {params.row.email}
            </Typography>
          </Box>
        </Stack>
      ),
    },
    {
      field: 'role',
      headerName: 'Role',
      flex: 0.6,
      minWidth: 130,
      renderCell: (params) => (
        <Chip size="small" label={params.value} color={ROLE_COLOR[params.value] ?? 'default'} variant="outlined" />
      ),
    },
    {
      field: 'createdAt',
      headerName: 'Joined',
      flex: 0.7,
      minWidth: 130,
      valueGetter: (value: string) => (value ? format(new Date(value), 'MMM d, yyyy') : '—'),
    },
    {
      field: 'enabled',
      headerName: 'Active',
      flex: 0.5,
      minWidth: 100,
      sortable: false,
      renderCell: (params) => (
        <Tooltip
          title={
            params.row.email.toLowerCase() === currentUser?.email?.toLowerCase()
              ? 'You cannot deactivate your own account'
              : params.row.enabled
                ? 'Deactivate this staff member'
                : 'Activate this staff member'
          }
        >
          <span>
            <Switch
              checked={params.row.enabled}
              size="small"
              onChange={() => handleToggleStatus(params.row)}
              disabled={updateStatus.isPending || params.row.email.toLowerCase() === currentUser?.email?.toLowerCase()}
            />
          </span>
        </Tooltip>
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
        <Tooltip title="Reset this staff member's password">
          <IconButton size="small" onClick={() => handleResetPassword(params.row)}>
            <LockResetRoundedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      ),
    },
  ];

  if (!isAdmin) {
    return (
      <Box>
        <PageHeader title="Staff" subtitle="Manage your clinic's team." />
        <EmptyState
          title="Admins only"
          description="Only your clinic's administrator can view and manage staff accounts."
        />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title="Staff"
        subtitle="Manage your clinic's team: invite doctors and receptionists, deactivate accounts, and recover a locked-out login."
        actions={
          <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={() => setFormOpen(true)}>
            Invite Staff
          </Button>
        }
      />

      <Card>
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <BadgeRoundedIcon fontSize="small" color="action" />
          <TextField
            placeholder="Search staff by name, email, or role..."
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
        <DataGrid
          rows={filteredStaff}
          columns={columns}
          loading={isLoading}
          disableRowSelectionOnClick
          autoHeight
          getRowHeight={() => 'auto'}
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          sx={{ border: 'none', '& .MuiDataGrid-cell': { display: 'flex', alignItems: 'center', py: 1 } }}
        />
      </Card>

      <InviteStaffDialog
        open={formOpen}
        loading={inviteStaff.isPending}
        onClose={() => setFormOpen(false)}
        onSubmit={handleInvite}
      />

      <PasswordRevealDialog
        open={Boolean(resetTarget)}
        title={`New temporary password${resetTarget ? ` for ${resetTarget.fullName}` : ''}`}
        result={resetResult}
        onClose={() => {
          setResetTarget(null);
          setResetResult(null);
        }}
      />
    </Box>
  );
}
