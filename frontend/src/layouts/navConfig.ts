import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import MedicalServicesRoundedIcon from '@mui/icons-material/MedicalServicesRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import MenuBookRoundedIcon from '@mui/icons-material/MenuBookRounded';
import ChatRoundedIcon from '@mui/icons-material/ChatRounded';
import InsightsRoundedIcon from '@mui/icons-material/InsightsRounded';
import SettingsRoundedIcon from '@mui/icons-material/SettingsRounded';
import type { SvgIconComponent } from '@mui/icons-material';

export interface NavItem {
  label: string;
  path: string;
  icon: SvgIconComponent;
}

export const navItems: NavItem[] = [
  { label: 'Dashboard', path: '/', icon: DashboardRoundedIcon },
  { label: 'Patients', path: '/patients', icon: PeopleAltRoundedIcon },
  { label: 'Doctors', path: '/doctors', icon: MedicalServicesRoundedIcon },
  { label: 'Appointments', path: '/appointments', icon: EventAvailableRoundedIcon },
  { label: 'Knowledge Base', path: '/knowledge-base', icon: MenuBookRoundedIcon },
  { label: 'AI Chat', path: '/chat', icon: ChatRoundedIcon },
  { label: 'Analytics', path: '/analytics', icon: InsightsRoundedIcon },
  { label: 'Settings', path: '/settings', icon: SettingsRoundedIcon },
];
