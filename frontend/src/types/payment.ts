export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED';

export interface Payment {
  id: string;
  tenantId: string;
  tenantName: string | null;
  amount: number;
  currency: string;
  description: string | null;
  status: PaymentStatus;
  createdBy: string;
  paidAt: string | null;
  createdAt: string;
}

/** Super-admin request to raise a new invoice against a tenant. */
export interface CreatePaymentRequest {
  tenantId: string;
  amount: number;
  currency: string;
  description?: string;
}

/** Shared by both the super-admin and clinic history views. */
export interface PaymentFilters {
  tenantId?: string;
  status?: PaymentStatus;
  from?: string;
  to?: string;
  minAmount?: number;
  maxAmount?: number;
}

export interface PaymentCheckoutResponse {
  paymentId: string;
  razorpayOrderId: string;
  razorpayKeyId: string;
  amountInMinorUnits: number;
  currency: string;
  description: string | null;
}

export interface PaymentVerifyRequest {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}
