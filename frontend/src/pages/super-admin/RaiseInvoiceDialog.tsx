import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from '@mui/material';
import Grid from '@mui/material/Grid2';
import type { CreatePaymentRequest } from '../../types/payment';
import type { Tenant } from '../../types/tenant';

const CURRENCY_OPTIONS = ['INR', 'USD', 'EUR', 'GBP', 'AED', 'SGD'];

const invoiceSchema = z.object({
  tenantId: z.string().min(1, 'Select a clinic'),
  amount: z.coerce.number().positive('Amount must be greater than zero'),
  currency: z.string().min(1, 'Select a currency'),
  description: z.string().max(500).optional(),
});

type InvoiceFormValues = z.infer<typeof invoiceSchema>;

interface RaiseInvoiceDialogProps {
  open: boolean;
  loading?: boolean;
  tenants: Tenant[];
  onClose: () => void;
  onSubmit: (values: CreatePaymentRequest) => void;
}

export function RaiseInvoiceDialog({ open, loading, tenants, onClose, onSubmit }: RaiseInvoiceDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<InvoiceFormValues>({
    resolver: zodResolver(invoiceSchema),
    defaultValues: { tenantId: '', amount: 0, currency: 'INR', description: '' },
  });

  useEffect(() => {
    if (open) {
      reset({ tenantId: '', amount: 0, currency: 'INR', description: '' });
    }
  }, [open, reset]);

  const submit = handleSubmit((values) =>
    onSubmit({ ...values, amount: Number(values.amount), description: values.description || undefined }),
  );

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Raise a new invoice</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={12}>
            <Controller
              name="tenantId"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  select
                  label="Clinic"
                  fullWidth
                  autoFocus
                  error={Boolean(errors.tenantId)}
                  helperText={errors.tenantId?.message}
                >
                  {tenants.map((tenant) => (
                    <MenuItem key={tenant.id} value={tenant.id}>
                      {tenant.name}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Grid>
          <Grid size={{ xs: 8 }}>
            <Controller
              name="amount"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  type="number"
                  label="Amount"
                  fullWidth
                  slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                  error={Boolean(errors.amount)}
                  helperText={errors.amount?.message}
                />
              )}
            />
          </Grid>
          <Grid size={{ xs: 4 }}>
            <Controller
              name="currency"
              control={control}
              render={({ field }) => (
                <TextField {...field} select label="Currency" fullWidth error={Boolean(errors.currency)}>
                  {CURRENCY_OPTIONS.map((code) => (
                    <MenuItem key={code} value={code}>
                      {code}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Description (optional)"
                  fullWidth
                  multiline
                  minRows={2}
                  placeholder="e.g. Monthly platform fee - January"
                  error={Boolean(errors.description)}
                  helperText={errors.description?.message}
                />
              )}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          Cancel
        </Button>
        <Button onClick={submit} variant="contained" loading={loading}>
          Raise invoice
        </Button>
      </DialogActions>
    </Dialog>
  );
}
