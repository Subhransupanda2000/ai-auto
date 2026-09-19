import { useState } from 'react';
import { useSnackbar } from 'notistack';
import { Card, Chip, MenuItem, Stack, TextField, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { format } from 'date-fns';
import { PageHeader } from '../../components/common/PageHeader';
import { useSuperAdminEnquiries, useUpdateEnquiryStatus } from '../../hooks/useSuperAdminEnquiries';
import type { Enquiry, EnquiryStatus } from '../../types/enquiry';

const STATUS_OPTIONS: { value: EnquiryStatus | ''; label: string }[] = [
  { value: '', label: 'Any status' },
  { value: 'NEW', label: 'New' },
  { value: 'CONTACTED', label: 'Contacted' },
  { value: 'CONVERTED', label: 'Converted' },
  { value: 'CLOSED', label: 'Closed' },
];

const STATUS_COLOR: Record<EnquiryStatus, 'info' | 'warning' | 'success' | 'default'> = {
  NEW: 'info',
  CONTACTED: 'warning',
  CONVERTED: 'success',
  CLOSED: 'default',
};

export function EnquiriesPage() {
  const { enqueueSnackbar } = useSnackbar();
  const [status, setStatus] = useState<EnquiryStatus | ''>('');
  const { data: enquiries = [], isLoading } = useSuperAdminEnquiries(status || undefined);
  const updateStatus = useUpdateEnquiryStatus();

  const handleStatusChange = (enquiry: Enquiry, newStatus: EnquiryStatus) => {
    updateStatus.mutate(
      { id: enquiry.id, status: newStatus },
      {
        onSuccess: () => enqueueSnackbar(`Marked "${enquiry.fullName}" as ${newStatus.toLowerCase()}`, { variant: 'success' }),
        onError: (error) => enqueueSnackbar(error.message, { variant: 'error' }),
      },
    );
  };

  const columns: GridColDef<Enquiry>[] = [
    { field: 'fullName', headerName: 'Name', flex: 0.9, minWidth: 160 },
    { field: 'email', headerName: 'Email', flex: 1, minWidth: 200 },
    { field: 'phone', headerName: 'Phone', flex: 0.7, minWidth: 130, valueGetter: (value: string | null) => value ?? '—' },
    {
      field: 'clinicName',
      headerName: 'Clinic',
      flex: 0.9,
      minWidth: 160,
      valueGetter: (value: string | null) => value ?? '—',
    },
    {
      field: 'message',
      headerName: 'Message',
      flex: 1.3,
      minWidth: 200,
      valueGetter: (value: string | null) => value ?? '—',
    },
    {
      field: 'status',
      headerName: 'Status',
      flex: 0.7,
      minWidth: 160,
      sortable: false,
      renderCell: (params) => (
        <TextField
          select
          size="small"
          variant="standard"
          value={params.row.status}
          onChange={(e) => handleStatusChange(params.row, e.target.value as EnquiryStatus)}
          disabled={updateStatus.isPending}
          sx={{ minWidth: 130 }}
        >
          {(['NEW', 'CONTACTED', 'CONVERTED', 'CLOSED'] as EnquiryStatus[]).map((value) => (
            <MenuItem key={value} value={value}>
              <Chip size="small" label={value} color={STATUS_COLOR[value]} />
            </MenuItem>
          ))}
        </TextField>
      ),
    },
    {
      field: 'createdAt',
      headerName: 'Received',
      flex: 0.7,
      minWidth: 150,
      valueGetter: (value: string) => (value ? format(new Date(value), 'MMM d, yyyy h:mm a') : '—'),
    },
  ];

  return (
    <>
      <PageHeader
        title="Demo Enquiries"
        subtitle="Leads raised via the public 'Request a Demo' form - reach out and track them through to conversion."
      />

      <Card>
        <Stack
          direction="row"
          spacing={2}
          alignItems="center"
          sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}
        >
          <Typography variant="body2" color="text.secondary">
            Filter:
          </Typography>
          <TextField
            select
            size="small"
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value as EnquiryStatus | '')}
            sx={{ minWidth: 160 }}
          >
            {STATUS_OPTIONS.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
        <DataGrid
          rows={enquiries}
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
