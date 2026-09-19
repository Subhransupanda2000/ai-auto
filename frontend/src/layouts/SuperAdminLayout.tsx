import { AppBar, Avatar, Box, Button, Stack, Toolbar, Typography } from '@mui/material';
import ShieldRoundedIcon from '@mui/icons-material/ShieldRounded';
import LogoutRoundedIcon from '@mui/icons-material/LogoutRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import PaidRoundedIcon from '@mui/icons-material/PaidRounded';
import ContactMailRoundedIcon from '@mui/icons-material/ContactMailRounded';
import { NavLink, Outlet } from 'react-router-dom';
import { useSuperAdminAuth } from '../hooks/useSuperAdminAuth';

const NAV_LINKS = [
  { label: 'Tenants', path: '/super-admin/tenants', icon: GroupsRoundedIcon },
  { label: 'Payments', path: '/super-admin/payments', icon: PaidRoundedIcon },
  { label: 'Enquiries', path: '/super-admin/enquiries', icon: ContactMailRoundedIcon },
];

/**
 * Minimal shell for the platform super-admin surface - deliberately
 * separate from {@code MainLayout} (no clinic sidebar/nav): a super admin
 * has no tenant and only manages tenant onboarding.
 */
export function SuperAdminLayout() {
  const { email, logout } = useSuperAdminAuth();

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" color="inherit" elevation={0} sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
        <Toolbar sx={{ gap: 1.5 }}>
          <Avatar sx={{ bgcolor: 'secondary.main', width: 34, height: 34 }}>
            <ShieldRoundedIcon fontSize="small" />
          </Avatar>
          <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
            Platform Admin
          </Typography>
          <Stack direction="row" spacing={0.5} sx={{ ml: 3 }}>
            {NAV_LINKS.map(({ label, path, icon: Icon }) => (
              <Button
                key={path}
                component={NavLink}
                to={path}
                size="small"
                startIcon={<Icon fontSize="small" />}
                sx={{
                  color: 'text.secondary',
                  '&.active': { color: 'primary.main', bgcolor: (theme) => `${theme.palette.primary.main}14` },
                }}
              >
                {label}
              </Button>
            ))}
          </Stack>
          <Box sx={{ flexGrow: 1 }} />
          <Stack direction="row" spacing={2} alignItems="center">
            <Typography variant="body2" color="text.secondary">
              {email}
            </Typography>
            <Button size="small" color="inherit" startIcon={<LogoutRoundedIcon fontSize="small" />} onClick={logout}>
              Sign out
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>
      <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1400, mx: 'auto' }}>
        <Outlet />
      </Box>
    </Box>
  );
}
