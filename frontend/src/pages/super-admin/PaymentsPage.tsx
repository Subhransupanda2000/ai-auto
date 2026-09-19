import { useMemo, useState } from 'react';
import { useSnackbar } from 'notistack';
import { Button, Card, IconButton, MenuItem, Stack, TextField, Tooltip, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { ConfirmDialog } from '../../components/common/ConfirmDialog';
import { PaymentStatusChip } from '../../components/common/PaymentStatusChip';
import { RaiseInvoiceDialog } from './RaiseInvoiceDialog';
import { useCancelPayment, useCreatePayment, useSuperAdminPayments } from '../../hooks/useSuperAdminPayments';
import { useTenants } from '../../hooks/useTenants';
import { formatCurrency } from '../../utils/currency';
import type { CreatePaymentRequest, Payment, PaymentFilters, PaymentStatus } from '../../types/payment';

const STATUS_OPTIONS: { value: PaymentStatus | ''; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'PAID', label: 'Paid' },
  { value: 'FAILED', label: 'Failed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

export function PaymentsPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { data: tenants = [] } = useTenants();

  const [tenantId, setTenantId] = useState('');
  const [status, setStatus] = useState<PaymentStatus | ''>('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [minAmount, setMinAmount] = useState('');
  const [maxAmount, setMaxAmount] = useState('');

  const filters: PaymentFilters = useMemo(
    () => ({
      tenantId: tenantId || undefined,
      status: status || undefined,
      from: from ? new Date(from).toISOString() : undefined,
      to: to ? new Date(new Date(to).setHours(23, 59, 59, 999)).toISOString() : undefined,
      minAmount: minAmount ? Number(minAmount) : undefined,
      maxAmount: maxAmount ? Number(maxAmount) : undefined,
    }),
    [tenantId, status, from, to, minAmount, maxAmount],
  );

  const { data: payments = [], isLoading } = useSuperAdminPayments(filters);
  const createPayment = useCreatePayment();
  const cancelPayment = useCancelPayment();

  const [formOpen, setFormOpen] = useState(false);
  const [cancelTarget, setCancelTarget] = useState<Payment | null>(null);

  const handleCreate = (values: CreatePaymentRequest) => {
    createPayment.mutate(values, {
      onSuccess: () => {
        enqueueSnackbar('Invoice raised', { variant: 'success' });
        setFormOpen(false);
      },
      onError: (error) => enqueueSnackbar(error.message, { variant: 'error' }),
    });
  };

  const handleCancel = () => {
    if (!cancelTarget) return;
    cancelPayment.mutate(cancelTarget.id, {
      onSuccess: () => {
        enqueueSnackbar('Invoice cancelled', { variant: 'success' });
        setCancelTarget(null);
      },
      onError: (error) => {
        enqueueSnackbar(error.message, { variant: 'error' });
        setCancelTarget(null);
      },
    });
  };

  const columns: GridColDef<Payment>[] = [
    { field: 'tenantName', headerName: 'Clinic', flex: 1, minWidth: 160 },
    {
      field: 'amount',
      headerName: 'Amount',
      flex: 0.7,
      minWidth: 130,
      renderCell: (params) => (
        <Typography variant="body2" fontWeight={600}>
          {formatCurrency(params.row.amount, params.row.currency)}
        </Typography>
      ),
    },
    { field: 'description', headerName: 'Description', flex: 1.2, minWidth: 180 },
    {
      field: 'status',
      headerName: 'Status',
      flex: 0.55,
      minWidth: 110,
      sortable: false,
      renderCell: (params) => <PaymentStatusChip status={params.row.status} />,
    },
    { field: 'createdBy', headerName: 'Raised by', flex: 0.8, minWidth: 160 },
    {
      field: 'createdAt',
      headerName: 'Raised',
      flex: 0.7,
      minWidth: 140,
      valueGetter: (value: string) => (value ? format(new Date(value), 'MMM d, yyyy') : '—'),
    },
    {
      field: 'paidAt',
      headerName: 'Paid',
      flex: 0.7,
      minWidth: 140,
      valueGetter: (value: string | null) => (value ? format(new Date(value), 'MMM d, yyyy') : '—'),
    },
    {
      field: 'actions',
      headerName: '',
      flex: 0.4,
      minWidth: 60,
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      renderCell: (params) =>
        params.row.status === 'PENDING' ? (
          <Tooltip title="Cancel this invoice">
            <IconButton size="small" onClick={() => setCancelTarget(params.row)}>
              <CancelRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        ) : null,
    },
  ];

  return (
    <>
      <PageHeader
        title="Payments"
        subtitle="Raise invoices against clinic tenants and review the full cross-tenant payment history."
        actions={
          <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={() => setFormOpen(true)}>
            Raise Invoice
          </Button>
        }
      />

      <Card>
        <Stack
          direction="row"
          spacing={2}
          alignItems="center"
          flexWrap="wrap"
          sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', gap: 2 }}
        >
          <TextField
            select
            size="small"
            label="Clinic"
            value={tenantId}
            onChange={(e) => setTenantId(e.target.value)}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Any clinic</MenuItem>
            {tenants.map((tenant) => (
              <MenuItem key={tenant.id} value={tenant.id}>
                {tenant.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            size="small"
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value as PaymentStatus | '')}
            sx={{ minWidth: 150 }}
          >
            {STATUS_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            size="small"
            type="date"
            label="From"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
            sx={{ minWidth: 150 }}
          />
          <TextField
            size="small"
            type="date"
            label="To"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
            sx={{ minWidth: 150 }}
          />
          <TextField
            size="small"
            type="number"
            label="Min amount"
            value={minAmount}
            onChange={(e) => setMinAmount(e.target.value)}
            sx={{ minWidth: 130 }}
          />
          <TextField
            size="small"
            type="number"
            label="Max amount"
            value={maxAmount}
            onChange={(e) => setMaxAmount(e.target.value)}
            sx={{ minWidth: 130 }}
          />
        </Stack>
        <DataGrid
          rows={payments}
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

      <RaiseInvoiceDialog
        open={formOpen}
        loading={createPayment.isPending}
        tenants={tenants}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreate}
      />

      <ConfirmDialog
        open={Boolean(cancelTarget)}
        title="Cancel this invoice?"
        message={`This will cancel the ${cancelTarget ? formatCurrency(cancelTarget.amount, cancelTarget.currency) : ''} invoice for "${cancelTarget?.tenantName ?? ''}". This cannot be undone.`}
        confirmLabel="Cancel invoice"
        destructive
        loading={cancelPayment.isPending}
        onConfirm={handleCancel}
        onClose={() => setCancelTarget(null)}
      />
    </>
  );
}
