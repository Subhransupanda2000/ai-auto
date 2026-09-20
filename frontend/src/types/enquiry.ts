export type EnquiryStatus = 'NEW' | 'CONTACTED' | 'CONVERTED' | 'CLOSED';

export interface Enquiry {
  id: string;
  fullName: string;
  email: string;
  phone: string | null;
  clinicName: string | null;
  message: string | null;
  status: EnquiryStatus;
  createdAt: string;
}

export interface CreateEnquiryRequest {
  fullName: string;
  email: string;
  phone: string;
  clinicName?: string;
  message?: string;
}

/** Same shape as LoginResponse (minus a refresh token) - see
 * DemoSignupResponse on the backend. */
export interface DemoSignupResponse {
  enquiry: Enquiry;
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface UpdateEnquiryStatusRequest {
  status: EnquiryStatus;
}
