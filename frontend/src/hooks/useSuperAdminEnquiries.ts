import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { superAdminApi } from '../api/superAdminApi';
import type { ApiError } from '../types/common';
import type { Enquiry, EnquiryStatus } from '../types/enquiry';

const enquiriesQueryKey = (status?: EnquiryStatus) => ['super-admin', 'enquiries', status] as const;

export function useSuperAdminEnquiries(status?: EnquiryStatus) {
  return useQuery({
    queryKey: enquiriesQueryKey(status),
    queryFn: () => superAdminApi.listEnquiries(status),
  });
}

export function useUpdateEnquiryStatus() {
  const queryClient = useQueryClient();
  return useMutation<Enquiry, ApiError, { id: string; status: EnquiryStatus }>({
    mutationFn: ({ id, status }) => superAdminApi.updateEnquiryStatus(id, { status }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['super-admin', 'enquiries'] }),
  });
}
