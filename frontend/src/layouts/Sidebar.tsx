import {
  Avatar,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
  Tooltip,
  Toolbar,
  Typography,
} from '@mui/material';
import LocalHospitalRoundedIcon from '@mui/icons-material/LocalHospitalRounded';
import ChevronLeftRoundedIcon from '@mui/icons-material/ChevronLeftRounded';
import ChevronRightRoundedIcon from '@mui/icons-material/ChevronRightRounded';
import LogoutRoundedIcon from '@mui/icons-material/LogoutRounded';
import { useEffect } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { getNavSectionsForRole } from './navConfig';
import { useSidebarStore } from '../store/sidebarStore';
import { useAuth } from '../hooks/useAuth';

const DEFAULT_BRAND_NAME = 'HealthcareAI';

export const SIDEBAR_WIDTH = 264;
export const SIDEBAR_RAIL_WIDTH = 84;

interface SidebarProps {
  variant: 'permanent' | 'temporary';
  open: boolean;
  onClose: () => void;
}

export function Sidebar({ variant, open, onClose }: SidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { collapsed, toggleCollapsed } = useSidebarStore();
  const navSections = getNavSectionsForRole(user?.role);

  // The sidebar only collapses to a slim icon rail on desktop; the mobile
  // drawer always shows the full, labeled layout.
  const isCollapsed = variant === 'permanent' && collapsed;
  const width = isCollapsed ? SIDEBAR_RAIL_WIDTH : SIDEBAR_WIDTH;
  const initials = (user?.email ?? '?').slice(0, 2).toUpperCase();
  const brandName = user?.tenantName ?? DEFAULT_BRAND_NAME;

  // Reflects the signed-in clinic in the browser tab too.
  useEffect(() => {
    document.title = user?.tenantName ? `${user.tenantName} · HealthcareAI` : 'HealthcareAI Console';
  }, [user?.tenantName]);

  const content = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar sx={{ px: isCollapsed ? 1.5 : 3, py: 2.5, justifyContent: isCollapsed ? 'center' : 'flex-start' }}>
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ minWidth: 0, overflow: 'hidden' }}>
          <Avatar
            sx={{
              width: 36,
              height: 36,
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #3B6FE0, #0FB5A7)',
              flexShrink: 0,
            }}
          >
            <LocalHospitalRoundedIcon fontSize="small" />
          </Avatar>
          {!isCollapsed && (
            <Box sx={{ minWidth: 0 }}>
              <Tooltip title={brandName} placement="right" disableHoverListener={brandName.length < 22}>
                <Typography variant="subtitle1" lineHeight={1.1} noWrap>
                  {brandName}
                </Typography>
              </Tooltip>
              <Typography variant="caption" color="text.secondary" noWrap>
                Clinic Console
              </Typography>
            </Box>
          )}
        </Stack>
      </Toolbar>
      <Divider />

      <List sx={{ px: isCollapsed ? 1 : 1.5, py: 2, flexGrow: 1, overflowY: 'auto' }}>
        {navSections.map((section, sectionIndex) => (
          <Box key={section.label} sx={{ mb: sectionIndex === navSections.length - 1 ? 0 : 1.5 }}>
            {!isCollapsed && (
              <Typography
                variant="caption"
                sx={{
                  display: 'block',
                  px: 1.5,
                  mb: 0.5,
                  mt: sectionIndex === 0 ? 0 : 1,
                  color: 'text.disabled',
                  fontWeight: 700,
                  letterSpacing: 1,
                  textTransform: 'uppercase',
                  fontSize: 11,
                }}
              >
                {section.label}
              </Typography>
            )}
            {section.items.map((item) => {
              const selected =
                item.path === '/' ? location.pathname === '/' : location.pathname.startsWith(item.path);
              const Icon = item.icon;
              const button = (
                <ListItemButton
                  key={item.path}
                  component={NavLink}
                  to={item.path}
                  onClick={onClose}
                  selected={selected}
                  sx={{
                    borderRadius: 2.5,
                    mb: 0.5,
                    py: 1,
                    justifyContent: isCollapsed ? 'center' : 'flex-start',
                    px: isCollapsed ? 1 : 1.5,
                    position: 'relative',
                    '&.Mui-selected': {
                      bgcolor: (theme) => `${theme.palette.primary.main}14`,
                      color: 'primary.main',
                      '& .MuiListItemIcon-root': { color: 'primary.main' },
                      '&::before': {
                        content: '""',
                        position: 'absolute',
                        left: isCollapsed ? 8 : 0,
                        top: '20%',
                        bottom: '20%',
                        width: 3,
                        borderRadius: 4,
                        bgcolor: 'primary.main',
                      },
                      '&:hover': { bgcolor: (theme) => `${theme.palette.primary.main}1F` },
                    },
                  }}
                >
                  <ListItemIcon sx={{ minWidth: isCollapsed ? 0 : 40, justifyContent: 'center' }}>
                    <Icon fontSize="small" />
                  </ListItemIcon>
                  {!isCollapsed && (
                    <ListItemText primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }}>
                      {item.label}
                    </ListItemText>
                  )}
                </ListItemButton>
              );

              return isCollapsed ? (
                <Tooltip key={item.path} title={item.label} placement="right">
                  {button}
                </Tooltip>
              ) : (
                button
              );
            })}
          </Box>
        ))}
      </List>

      {variant === 'permanent' && (
        <>
          <Divider />
          <Box sx={{ display: 'flex', justifyContent: isCollapsed ? 'center' : 'flex-end', p: 1 }}>
            <Tooltip title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'} placement="right">
              <IconButton size="small" onClick={toggleCollapsed}>
                {isCollapsed ? <ChevronRightRoundedIcon fontSize="small" /> : <ChevronLeftRoundedIcon fontSize="small" />}
              </IconButton>
            </Tooltip>
          </Box>
        </>
      )}

      <Divider />
      <Box sx={{ p: isCollapsed ? 1 : 1.5 }}>
        <Stack
          direction="row"
          spacing={1.25}
          alignItems="center"
          sx={{
            p: isCollapsed ? 0.5 : 1,
            borderRadius: 2.5,
            justifyContent: isCollapsed ? 'center' : 'flex-start',
          }}
        >
          <Tooltip title={isCollapsed ? (user?.email ?? 'Account') : ''} placement="right">
            <Avatar
              onClick={() => navigate('/settings')}
              sx={{ width: 36, height: 36, bgcolor: 'secondary.main', fontSize: 14, cursor: 'pointer', flexShrink: 0 }}
            >
              {initials}
            </Avatar>
          </Tooltip>
          {!isCollapsed && (
            <>
              <Box sx={{ minWidth: 0, flexGrow: 1 }}>
                <Typography variant="body2" fontWeight={700} noWrap>
                  {user?.email}
                </Typography>
                <Typography variant="caption" color="text.secondary" noWrap>
                  {user?.role}
                </Typography>
              </Box>
              <Tooltip title="Sign out">
                <IconButton size="small" onClick={logout}>
                  <LogoutRoundedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </>
          )}
        </Stack>
        {!isCollapsed && (
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', px: 1, mt: 1 }}>
            © {new Date().getFullYear()} HealthcareAI
          </Typography>
        )}
      </Box>
    </Box>
  );

  return (
    <Drawer
      variant={variant}
      open={variant === 'permanent' ? true : open}
      onClose={onClose}
      ModalProps={{ keepMounted: true }}
      sx={{
        width,
        flexShrink: 0,
        transition: (theme) => theme.transitions.create('width'),
        [`& .MuiDrawer-paper`]: {
          width,
          boxSizing: 'border-box',
          borderRight: '1px solid',
          borderColor: 'divider',
          overflowX: 'hidden',
          transition: (theme) => theme.transitions.create('width'),
        },
      }}
    >
      {content}
    </Drawer>
  );
}
