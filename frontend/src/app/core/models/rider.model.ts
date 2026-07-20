export interface RiderProfile {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  paymentMethodLabel: string | null;
  paymentMethodLast4: string | null;
  rating: number;
  totalTrips: number;
}
