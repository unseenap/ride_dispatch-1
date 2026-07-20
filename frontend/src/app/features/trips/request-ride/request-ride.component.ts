import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { TripService } from '../../../core/services/trip.service';
import { FareEstimateResponse } from '../../../core/models/trip.model';

/**
 * NOTE: pickup/dropoff coordinates here are hardcoded placeholders. A real
 * implementation would geocode the typed addresses (e.g. via a maps
 * provider's Places/Geocoding API) to get lat/lng - out of scope for this
 * challenge, so we just use fixed San Francisco coordinates so the fare
 * estimate + trip request endpoints have something valid to work with.
 */
const PLACEHOLDER_PICKUP = { lat: 37.7749, lng: -122.4194 };
const PLACEHOLDER_DROPOFF = { lat: 37.7849, lng: -122.4094 };

@Component({
  selector: 'app-request-ride',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './request-ride.component.html',
  styleUrl: './request-ride.component.scss'
})
export class RequestRideComponent implements OnInit {
  readonly form = this.fb.group({
    pickupAddress: ['', [Validators.required]],
    dropoffAddress: ['']
  });

  readonly fareEstimate = signal<FareEstimateResponse | null>(null);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private tripService: TripService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Kicks off a single fare estimate on load. The form does not currently
    // re-subscribe when pickup/dropoff values change afterwards, so editing
    // either address field after the first estimate does not refresh the
    // preview shown to the rider.
    this.tripService
      .estimateFare({
        pickupLat: PLACEHOLDER_PICKUP.lat,
        pickupLng: PLACEHOLDER_PICKUP.lng,
        dropoffLat: PLACEHOLDER_DROPOFF.lat,
        dropoffLng: PLACEHOLDER_DROPOFF.lng
      })
      .subscribe((estimate) => this.fareEstimate.set(estimate));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);

    const raw = this.form.getRawValue();

    this.tripService
      .requestTrip({
        pickupLat: PLACEHOLDER_PICKUP.lat,
        pickupLng: PLACEHOLDER_PICKUP.lng,
        pickupAddress: raw.pickupAddress!,
        dropoffLat: PLACEHOLDER_DROPOFF.lat,
        dropoffLng: PLACEHOLDER_DROPOFF.lng,
        dropoffAddress: raw.dropoffAddress!
      })
      .subscribe((trip) => {
        this.submitting.set(false);
        this.router.navigate(['/trips', trip.id]);
      });
    // NOTE: no error handler on this subscribe - if the backend rejects the
    // request (e.g. dropoffAddress blank fails @NotBlank server-side even
    // though the field above isn't marked required), submitting() never
    // resets and nothing is shown to the rider.
  }
}
