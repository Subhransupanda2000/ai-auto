import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, TextField, Typography } from '@mui/material';
import Grid from '@mui/material/Grid2';
import type { CreateTenantRequest } from '../../types/tenant';

const slugPattern = /^[a-z0-9]+(-[a-z0-9]+)*$/;

const tenantSchema = z.object({
  tenantName: z.string().min(1, 'Clinic name is required'),
  slug: z
    .string()
    .min(1, 'Slug is required')
    .regex(slugPattern, "Lowercase letters, numbers, and hyphens only, e.g. 'sunrise-clinic'"),
  adminFullName: z.string().min(1, "Admin's full name is required"),
  adminEmail: z.string().min(1, 'Admin email is required').email('Enter a valid email address'),
  adminPassword: z.string().min(8, 'Password must be at least 8 characters'),
});

type TenantFormValues = z.infer<typeof tenantSchema>;

function slugify(value: string): string {
  return value
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

interface CreateTenantDialogProps {
  open: boolean;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: CreateTenantRequest) => void;
}

export function CreateTenantDialog({ open, loading, onClose, onSubmit }: CreateTenantDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    setValue,
    getFieldState,
    formState: { errors },
  } = useForm<TenantFormValues>({
    resolver: zodResolver(tenantSchema),
    defaultValues: { tenantName: '', slug: '', adminFullName: '', adminEmail: '', adminPassword: '' },
  });

  useEffect(() => {
    if (open) {
      reset({ tenantName: '', slug: '', adminFullName: '', adminEmail: '', adminPassword: '' });
    }
  }, [open, reset]);

  const submit = handleSubmit((values) => onSubmit(values));

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Onboard a new clinic</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={12}>
            <Controller
              name="tenantName"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Clinic name"
                  fullWidth
                  autoFocus
                  error={Boolean(errors.tenantName)}
                  helperText={errors.tenantName?.message}
                  onChange={(e) => {
                    field.onChange(e);
                    // Keep the slug in sync unless the user has already
                    // hand-edited it themselves.
                    if (!getFieldState('slug').isDirty) {
                      setValue('slug', slugify(e.target.value));
                    }
                  }}
                />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="slug"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Slug"
                  fullWidth
                  helperText={errors.slug?.message ?? "Used as this clinic's unique identifier, e.g. in the chat widget URL."}
                  error={Boolean(errors.slug)}
                />
              )}
            />
          </Grid>

          <Grid size={12}>
            <Divider sx={{ my: 1 }}>
              <Typography variant="caption" color="text.secondary">
                First administrator
              </Typography>
            </Divider>
          </Grid>

          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="adminFullName"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Admin full name"
                  fullWidth
                  error={Boolean(errors.adminFullName)}
                  helperText={errors.adminFullName?.message}
                />
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="adminEmail"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Admin email"
                  fullWidth
                  error={Boolean(errors.adminEmail)}
                  helperText={errors.adminEmail?.message}
                />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="adminPassword"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  type="password"
                  label="Admin temporary password"
                  fullWidth
                  error={Boolean(errors.adminPassword)}
                  helperText={errors.adminPassword?.message ?? 'Share this with the clinic admin so they can sign in and change it.'}
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
          Create clinic
        </Button>
      </DialogActions>
    </Dialog>
  );
}
