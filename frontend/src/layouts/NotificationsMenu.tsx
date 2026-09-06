import { Box, Divider, ListItemIcon, ListItemText, Menu, MenuItem, Typography } from '@mui/material';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import PersonAddAltRoundedIcon from '@mui/icons-material/PersonAddAltRounded';
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';

interface NotificationsMenuProps {
  anchorEl: HTMLElement | null;
  onClose: () => void;
}

const notifications = [
  {
    icon: <EventAvailableRoundedIcon fontSize="small" color="primary" />,
    title: 'New appointment booked',
    detail: 'Sarah Johnson booked with Dr. Patel for 2:30 PM',
  },
  {
    icon: <PersonAddAltRoundedIcon fontSize="small" color="success" />,
    title: 'New patient registered',
    detail: 'Michael Chen joined via the AI receptionist',
  },
  {
    icon: <CancelRoundedIcon fontSize="small" color="error" />,
    title: 'Appointment cancelled',
    detail: 'Emma Davis cancelled her 4:00 PM visit',
  },
];

export function NotificationsMenu({ anchorEl, onClose }: NotificationsMenuProps) {
  return (
    <Menu
      anchorEl={anchorEl}
      open={Boolean(anchorEl)}
      onClose={onClose}
      transformOrigin={{ horizontal: 'right', vertical: 'top' }}
      anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
    >
      <Box sx={{ px: 2, py: 1.5, minWidth: 320 }}>
        <Typography variant="subtitle2">Notifications</Typography>
      </Box>
      <Divider />
      {notifications.map((n) => (
        <MenuItem key={n.title} onClick={onClose} sx={{ alignItems: 'flex-start', whiteSpace: 'normal', py: 1.25 }}>
          <ListItemIcon sx={{ mt: 0.25 }}>{n.icon}</ListItemIcon>
          <ListItemText
            primary={n.title}
            secondary={n.detail}
            primaryTypographyProps={{ fontSize: 13, fontWeight: 600 }}
            secondaryTypographyProps={{ fontSize: 12 }}
          />
        </MenuItem>
      ))}
    </Menu>
  );
}
