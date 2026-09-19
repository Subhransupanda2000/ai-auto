import { Alert, AlertTitle, Button, Stack, Typography } from '@mui/material';
import PlayCircleRoundedIcon from '@mui/icons-material/PlayCircleRounded';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

/** Shown across the top of every page for a read-only demo session (see
 * RequestDemoPage/DemoAuthenticationFilter) - the backend already hard-
 * blocks every write for this role, this is just making that visible so a
 * "Save"/"Create" click that quietly 403s isn't confusing. */
export function DemoModeBanner() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  return (
    <Alert
      severity="info"
      icon={<PlayCircleRoundedIcon fontSize="small" />}
      sx={{ borderRadius: 0 }}
      action={
        <Stack direction="row" spacing={1}>
          <Button
            size="small"
            variant="contained"
            onClick={() => {
              logout();
              navigate('/request-demo', { replace: true });
            }}
          >
            Get Your Own Clinic
          </Button>
        </Stack>
      }
    >
      <AlertTitle sx={{ mb: 0 }}>You're viewing a live, read-only demo</AlertTitle>
      <Typography variant="body2" color="text.secondary">
        This is real sample clinic data. Every action is view-only - creating, editing, and
        deleting are disabled for demo sessions.
      </Typography>
    </Alert>
  );
}
