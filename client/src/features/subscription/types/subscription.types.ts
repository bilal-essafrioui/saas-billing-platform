// checkout response type
export type CheckoutResponse = {
  clientSecret: string;
  paymentId: string;
  paymentIntentId: string;
};

export type SubscriptionStatus =
  | "ACTIVE"
  | "PAST_DUE"
  | "CANCELLED"
  | "SUSPENDED";

export interface SubscriptionResponse {
  id: string;

  // User
  userId: string;
  firstName: string;
  lastName: string;
  userEmail: string;

  // Plan
  planId: string;
  planName: string;
  planPrice: number;

  // Subscription
  status: SubscriptionStatus;
  startDate: string;
  nextRenewalDate: string;
  cancelledAt: string | null;

  // Pending downgrade
  pendingPlanId: string | null;
  pendingPlanName: string | null;
  pendingPlanEffectiveDate: string | null;

  createdAt: string;
  updatedAt: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

export type SubscriptionEventType =
  | "SUBSCRIBED"
  | "UPGRADED"
  | "DOWNGRADE_SCHEDULED"
  | "DOWNGRADE_APPLIED"
  | "STATUS_CHANGED"
  | "CANCELLED"
  | "RESUBSCRIBED"
  | "SUSPENSION";

export interface SubscriptionEventResponse {
  id: string;

  eventType: SubscriptionEventType;

  previousPlanName: string | null;
  previousPlanPrice: number | null;

  newPlanName: string;
  newPlanPrice: number;

  previousStatus: SubscriptionStatus | null;
  newStatus: SubscriptionStatus;

  prorataAmount: number | null;

  note: string | null;

  createdAt: string;
}

export interface SubscriptionHistoryErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}