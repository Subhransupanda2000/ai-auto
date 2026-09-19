import { apiClient } from './client';
import type { CreateEnquiryRequest, DemoSignupResponse } from '../types/enquiry';

export const enquiryApi = {
  requestDemo: (payload: CreateEnquiryRequest) =>
    apiClient.post<DemoSignupResponse>('/enquiries', payload).then((res) => res.data),
};
