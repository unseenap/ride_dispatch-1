export type TripStatus =
  | 'REQUESTED'
  | 'ACCEPTED'
  | 'ARRIVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface TripStatusHistoryEntry {
  status: TripStatus;
  changedAt: string;
  note: string | null;
}

export interface Trip {
  id: number;

  riderId: number;
  riderName: string;

  driverId: number | null;
  driverName: string | null;
  driverVehicle: string | null;
  driverLicensePlate: string | null;

  pickupLat: number;
  pickupLng: number;
  pickupAddress: string;

  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress: string;

  status: TripStatus;

  requestedAt: string;
  acceptedAt: string | null;
  arrivedAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  cancellationReason: string | null;

  fareEstimate: number | null;
  finalFare: number | null;
  distanceKm: number | null;

  statusHistory: TripStatusHistoryEntry[];
}

export interface TripRequest {
  pickupLat: number;
  pickupLng: number;
  pickupAddress: string;
  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress: string;
}

export interface FareEstimateRequest {
  pickupLat: number;
  pickupLng: number;
  dropoffLat: number;
  dropoffLng: number;
}

export interface FareEstimateResponse {
  estimatedFare: number;
  distanceKm: number;
  estimatedDurationMinutes: number;
}

export interface CancelTripRequest {
  reason?: string;
}
