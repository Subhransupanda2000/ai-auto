import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import ContentCopyRoundedIcon from '@mui/icons-material/ContentCopyRounded';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';

export interface PasswordRevealResult {
  email: string;
  temporaryPassword: string;
}

interface PasswordRevealDialogProps {
  open: boolean;
  title: string;
  result: PasswordRevealResult | null;
  onClose: () => void;
}

function CopyField({ label, value }: { label: string; value: string }) {
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    await navigator.clipboard.writeText(value);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <TextField
      label={label}
      value={value}
      fullWidth
      size="small"
      slotProps={{
        input: {
          readOnly: true,
          endAdornment: (
            <Tooltip title={copied ? 'Copied!' : `Copy ${label.toLowerCase()}`}>
              <IconButton size="small" onClick={copy} edge="end">
                {copied ? <CheckRoundedIcon fontSize="small" color="success" /> : <ContentCopyRoundedIcon fontSize="small" />}
              </IconButton>
            </Tooltip>
          ),
        },
      }}
    />
  );
}

/**
 * One-time reveal of a freshly generated temporary password (tenant admin
 * reset by a super admin, or a staff member reset by their clinic's
 * admin). The value only ever exists in this dialog's props for the
 * lifetime of this render - it is never cached, refetched, or shown again
 * once closed, since the backend never stores it in plaintext either.
 */
export function PasswordRevealDialog({ open, title, result, onClose }: PasswordRevealDialogProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        {result ? (
          <Stack spacing={2}>
            <Alert severity="warning" variant="outlined">
              This password is shown only once and is not stored anywhere in readable form. Copy it now and
              relay it directly - they should change it after signing in.
            </Alert>
            <CopyField label="Email" value={result.email} />
            <CopyField label="Temporary password" value={result.temporaryPassword} />
          </Stack>
        ) : (
          <Box sx={{ py: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Generating a new password...
            </Typography>
          </Box>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} variant="contained">
          Done
        </Button>
      </DialogActions>
    </Dialog>
  );
}
