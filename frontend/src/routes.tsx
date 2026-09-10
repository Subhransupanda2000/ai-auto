import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { SuperAdminProtectedRoute } from './auth/SuperAdminProtectedRoute';
import { AuthWatcher } from './auth/AuthWatcher';
import { MainLayout } from './layouts/MainLayout';
import { SuperAdminLayout } from './layouts/SuperAdminLayout';
import { LoginPage } from './pages/login/LoginPage';
import { ForgotPasswordPage } from './pages/login/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/login/ResetPasswordPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { PatientsPage } from './pages/patients/PatientsPage';
import { PatientDetailPage } from './pages/patients/PatientDetailPage';
import { DoctorsPage } from './pages/doctors/DoctorsPage';
import { AppointmentsPage } from './pages/appointments/AppointmentsPage';
import { RevenuePage } from './pages/revenue/RevenuePage';
import { KnowledgeBasePage } from './pages/knowledge-base/KnowledgeBasePage';
import { ChatPage } from './pages/chat/ChatPage';
import { AnalyticsPage } from './pages/dashboard/AnalyticsPage';
import { SettingsPage } from './pages/settings/SettingsPage';
import { SuperAdminLoginPage } from './pages/super-admin/SuperAdminLoginPage';
import { TenantsPage } from './pages/super-admin/TenantsPage';
import { NotFoundPage } from './pages/NotFoundPage';

export function AppRoutes() {
  return (
    <>
      <AuthWatcher />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<DashboardPage />} />
          <Route path="/patients" element={<PatientsPage />} />
          <Route path="/patients/:id" element={<PatientDetailPage />} />
          <Route path="/doctors" element={<DoctorsPage />} />
          <Route path="/appointments" element={<AppointmentsPage />} />
          <Route path="/revenue" element={<RevenuePage />} />
          <Route path="/knowledge-base" element={<KnowledgeBasePage />} />
          <Route path="/chat" element={<ChatPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>

        {/* Platform super-admin surface: fully separate login/session from
            the clinic staff app above - only super admins can onboard
            tenants. */}
        <Route path="/super-admin/login" element={<SuperAdminLoginPage />} />
        <Route
          element={
            <SuperAdminProtectedRoute>
              <SuperAdminLayout />
            </SuperAdminProtectedRoute>
          }
        >
          <Route path="/super-admin" element={<Navigate to="/super-admin/tenants" replace />} />
          <Route path="/super-admin/tenants" element={<TenantsPage />} />
        </Route>

        <Route path="/404" element={<NotFoundPage />} />
        <Route path="*" element={<Navigate to="/404" replace />} />
      </Routes>
    </>
  );
}
