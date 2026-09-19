import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { enquiryApi } from '../api/enquiryApi';
import { useAuthStore } from '../store/authStore';
import type { ApiError } from '../types/common';
import type { CreateEnquiryRequest, DemoSignupResponse } from '../types/enquiry';

/** Submits the public "Request a Demo" form and, on success, drops the
 * returned read-only session straight into the same session store a real
 * login uses (no refresh token - a demo session simply expires) and lands
 * the visitor in the real app UI. */
export function useRequestDemo() {
  const { setSession } = useAuthStore();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation<DemoSignupResponse, ApiError, CreateEnquiryRequest>({
    mutationFn: (payload) => enquiryApi.requestDemo(payload),
    onSuccess: (response) => {
      queryClient.clear();
      setSession(response.accessToken, null);
      navigate('/', { replace: true });
    },
  });
}
