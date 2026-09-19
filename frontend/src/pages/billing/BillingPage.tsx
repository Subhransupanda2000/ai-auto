import { useMemo, useState } from 'react';
import { useSnackbar } from 'notistack';
import { Button, Card, MenuItem, Stack, TextField, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import PaymentRoundedIcon from '@mui/icons-material/PaymentRounded';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { PaymentStatusChip } from '../../components/common/PaymentStatusChip';
import { useAuth } from '../../hooks/useAuth';
import { useCheckoutPayment, usePayments, useVerifyPayment } from '../../hooks/usePayments';
import { openRazorpayCheckout } from '../../lib/razorpay';
import { formatCurrency } from '../../utils/currency';
import type { Payment, PaymentFilters, PaymentStatus } from '../../types/payment';

const STATUS_OPTIONS: { value: PaymentStatus | ''; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'PAID', label: 'Paid' },
  { value: 'FAILED', label: 'Failed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

export function BillingPage() {
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();

  const [status, setStatus] = useState<PaymentStatus | ''>('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [minAmount, setMinAmount] = useState('');
  const [maxAmount, setMaxAmount] = useState('');

  const filters: PaymentFilters = useMemo(
    () => ({
      status: status || undefined,
      from: from ? new Date(from).toISOString() : undefined,
      to: to ? new Date(new Date(to).setHours(23, 59, 59, 999)).toISOString() : undefined,
      minAmount: minAmount ? Number(minAmount) : undefined,
      maxAmount: maxAmount ? Number(maxAmount) : undefined,
    }),
    [status, from, to, minAmount, maxAmount],
  );

  const { data: payments = [], isLoading } = usePayments(filters);
  const checkout = useCheckoutPayment();
  const verify = useVerifyPayment();
  const [payingId, setPayingId] = useState<string | null>(null);

  const handlePay = (payment: Payment) => {
    setPayingId(payment.id);
    checkout.mutate(payment.id, {
      onSuccess: (order) => {
        openRazorpayCheckout({
          key: order.razorpayKeyId,
          amount: order.amountInMinorUnits,
          currency: order.currency,
          name: 'HealthcareAI',
          description: order.description ?? 'Clinic invoice payment',
          order_id: order.razorpayOrderId,
          prefill: { email: user?.email },
          theme: { color: '#3B6FE0' },
          handler: (response) => {
            verify.mutate(
              {
                id: payment.id,
                payload: {
                  razorpayOrderId: response.razorpay_order_id,
                  razorpayPaymentId: response.razorpay_payment_id,
                  razorpaySignature: response.razorpay_signature,
                },
              },
              {
                onSuccess: () => {
                  enqueueSnackbar('Payment successful', { variant: 'success' });
                  setPayingId(null);
                },
                onError: (error) => {
                  enqueueSnackbar(error.message, { variant: 'error' });
                  setPayingId(null);
                },
              },
            );
          },
          modal: { ondismiss: () => setPayingId(null) },
        }).catch((error: Error) => {
          enqueueSnackbar(error.message, { variant: 'error' });
          setPayingId(null);
        });
      },
      onError: (error) => {
        enqueueSnackbar(error.message, { variant: 'error' });
        setPayingId(null);
      },
    });
  };

  const columns: GridColDef<Payment>[] = [
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
    { field: 'description', headerName: 'Description', flex: 1.4, minWidth: 200 },
    {
      field: 'status',
      headerName: 'Status',
      flex: 0.55,
      minWidth: 110,
      sortable: false,
      renderCell: (params) => <PaymentStatusChip status={params.row.status} />,
    },
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
      flex: 0.6,
      minWidth: 130,
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      renderCell: (params) =>
        params.row.status === 'PENDING' ? (
          <Button
            size="small"
            variant="contained"
            startIcon={<PaymentRoundedIcon fontSize="small" />}
            loading={payingId === params.row.id && (checkout.isPending || verify.isPending)}
            onClick={() => handlePay(params.row)}
          >
            Pay Now
          </Button>
        ) : null,
    },
  ];

  return (
    <>
      <PageHeader
        title="Billing"
        subtitle="Invoices raised by HealthcareAI against your clinic, and your payment history."
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
    </>
  );
}
