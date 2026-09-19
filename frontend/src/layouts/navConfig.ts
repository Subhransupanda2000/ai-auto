import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import MedicalServicesRoundedIcon from '@mui/icons-material/MedicalServicesRounded';
import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import MenuBookRoundedIcon from '@mui/icons-material/MenuBookRounded';
import ChatRoundedIcon from '@mui/icons-material/ChatRounded';
import InsightsRoundedIcon from '@mui/icons-material/InsightsRounded';
import PaidRoundedIcon from '@mui/icons-material/PaidRounded';
import SettingsRoundedIcon from '@mui/icons-material/SettingsRounded';
import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import ReceiptLongRoundedIcon from '@mui/icons-material/ReceiptLongRounded';
import type { SvgIconComponent } from '@mui/icons-material';
import type { Role } from '../types/auth';

export interface NavItem {
  label: string;
  path: string;
  icon: SvgIconComponent;
  /** Restricts this item to the given roles; omit to show it to everyone
   * signed in. */
  roles?: Role[];
}

export interface NavSection {
  label: string;
  items: NavItem[];
}

export const navSections: NavSection[] = [
  {
    label: 'Overview',
    items: [{ label: 'Dashboard', path: '/', icon: DashboardRoundedIcon }],
  },
  {
    label: 'Clinic',
    items: [
      { label: 'Patients', path: '/patients', icon: PeopleAltRoundedIcon },
      { label: 'Doctors', path: '/doctors', icon: MedicalServicesRoundedIcon },
      { label: 'Appointments', path: '/appointments', icon: EventAvailableRoundedIcon },
      { label: 'Revenue', path: '/revenue', icon: PaidRoundedIcon },
    ],
  },
  {
    label: 'AI Tools',
    items: [
      { label: 'Knowledge Base', path: '/knowledge-base', icon: MenuBookRoundedIcon },
      { label: 'AI Chat', path: '/chat', icon: ChatRoundedIcon },
    ],
  },
  {
    label: 'Insights',
    items: [{ label: 'Analytics', path: '/analytics', icon: InsightsRoundedIcon }],
  },
  {
    label: 'Administration',
    items: [
      { label: 'Staff', path: '/staff', icon: BadgeRoundedIcon, roles: ['ADMIN'] },
      { label: 'Billing', path: '/billing', icon: ReceiptLongRoundedIcon, roles: ['ADMIN'] },
      // Backs onto GET /api/tenant/me, which is ADMIN-only - hidden for the
      // read-only demo role (see RequestDemoPage) so it doesn't 403.
      { label: 'Settings', path: '/settings', icon: SettingsRoundedIcon, roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
    ],
  },
];

/** Nav sections filtered down to the items a given role may see, dropping
 * any section left with no items. `role` may be undefined momentarily
 * while auth state is still hydrating. */
export function getNavSectionsForRole(role: Role | undefined): NavSection[] {
  return navSections
    .map((section) => ({
      ...section,
      items: section.items.filter((item) => !item.roles || (role && item.roles.includes(role))),
    }))
    .filter((section) => section.items.length > 0);
}

export const navItems: NavItem[] = navSections.flatMap((section) => section.items);
