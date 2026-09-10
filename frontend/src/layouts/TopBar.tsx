import { useState, type MouseEvent } from 'react';
import {
  AppBar,
  Avatar,
  Badge,
  Box,
  Divider,
  IconButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Stack,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import MenuRoundedIcon from '@mui/icons-material/MenuRounded';
import NotificationsRoundedIcon from '@mui/icons-material/NotificationsRounded';
import DarkModeRoundedIcon from '@mui/icons-material/DarkModeRounded';
import LightModeRoundedIcon from '@mui/icons-material/LightModeRounded';
import LogoutRoundedIcon from '@mui/icons-material/LogoutRounded';
import SettingsRoundedIcon from '@mui/icons-material/SettingsRounded';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import { useNavigate } from 'react-router-dom';
import { useThemeStore } from '../store/themeStore';
import { useAuth } from '../hooks/useAuth';
import { SIDEBAR_RAIL_WIDTH, SIDEBAR_WIDTH } from './Sidebar';
import { useSidebarStore } from '../store/sidebarStore';
import { NotificationsMenu } from './NotificationsMenu';

interface TopBarProps {
  onMenuClick: () => void;
}

export function TopBar({ onMenuClick }: TopBarProps) {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('lg'));
  const { mode, toggleMode } = useThemeStore();
  const { collapsed } = useSidebarStore();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const sidebarWidth = collapsed ? SIDEBAR_RAIL_WIDTH : SIDEBAR_WIDTH;

  const [profileAnchor, setProfileAnchor] = useState<HTMLElement | null>(null);
  const [notifAnchor, setNotifAnchor] = useState<HTMLElement | null>(null);

  const openProfile = (e: MouseEvent<HTMLElement>) => setProfileAnchor(e.currentTarget);
  const closeProfile = () => setProfileAnchor(null);
  const openNotifications = (e: MouseEvent<HTMLElement>) => setNotifAnchor(e.currentTarget);
  const closeNotifications = () => setNotifAnchor(null);

  const initials = (user?.email ?? '?').slice(0, 2).toUpperCase();

  return (
    <AppBar
      position="fixed"
      color="inherit"
      sx={{
        bgcolor: 'background.paper',
        width: isDesktop ? `calc(100% - ${sidebarWidth}px)` : '100%',
        ml: isDesktop ? `${sidebarWidth}px` : 0,
        transition: (theme) => theme.transitions.create(['width', 'margin']),
      }}
    >
      <Toolbar sx={{ gap: 1 }}>
        {!isDesktop && (
          <IconButton edge="start" onClick={onMenuClick} sx={{ mr: 1 }}>
            <MenuRoundedIcon />
          </IconButton>
        )}

        <Box sx={{ flexGrow: 1 }} />

        <Stack direction="row" spacing={0.5} alignItems="center">
          <Tooltip title={mode === 'light' ? 'Switch to dark mode' : 'Switch to light mode'}>
            <IconButton onClick={toggleMode}>
              {mode === 'light' ? <DarkModeRoundedIcon /> : <LightModeRoundedIcon />}
            </IconButton>
          </Tooltip>

          <Tooltip title="Notifications">
            <IconButton onClick={openNotifications}>
              <Badge badgeContent={3} color="error">
                <NotificationsRoundedIcon />
              </Badge>
            </IconButton>
          </Tooltip>

          <NotificationsMenu anchorEl={notifAnchor} onClose={closeNotifications} />

          <Tooltip title="Account">
            <IconButton onClick={openProfile} sx={{ ml: 0.5 }}>
              <Avatar sx={{ width: 34, height: 34, bgcolor: 'secondary.main', fontSize: 14 }}>
                {initials}
              </Avatar>
            </IconButton>
          </Tooltip>

          <Menu
            anchorEl={profileAnchor}
            open={Boolean(profileAnchor)}
            onClose={closeProfile}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            <Box sx={{ px: 2, py: 1.5, minWidth: 220 }}>
              <Typography variant="subtitle2">{user?.email}</Typography>
              <Typography variant="caption" color="text.secondary">
                {user?.role}
              </Typography>
            </Box>
            <Divider />
            <MenuItem
              onClick={() => {
                closeProfile();
                navigate('/settings');
              }}
            >
              <ListItemIcon>
                <PersonRoundedIcon fontSize="small" />
              </ListItemIcon>
              <ListItemText>Profile</ListItemText>
            </MenuItem>
            <MenuItem
              onClick={() => {
                closeProfile();
                navigate('/settings');
              }}
            >
              <ListItemIcon>
                <SettingsRoundedIcon fontSize="small" />
              </ListItemIcon>
              <ListItemText>Settings</ListItemText>
            </MenuItem>
            <Divider />
            <MenuItem onClick={logout}>
              <ListItemIcon>
                <LogoutRoundedIcon fontSize="small" />
              </ListItemIcon>
              <ListItemText>Sign out</ListItemText>
            </MenuItem>
          </Menu>
        </Stack>
      </Toolbar>
    </AppBar>
  );
}
