import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { interval, startWith, switchMap } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';

import { TripService } from '../../../core/services/trip.service';
import { Trip } from '../../../core/models/trip.model';
import { TripStatusChipComponent } from '../../shared/components/trip-status-chip.component';
import { formatTripTimestamp } from '../../../core/utils/date-format.util';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-trip-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    TripStatusChipComponent
  ],
  templateUrl: './trip-detail.component.html',
  styleUrl: './trip-detail.component.scss'
})
export class TripDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly notFound = signal(false);
  // Signal holding the latest polled state of the trip - a real-time detail
  // view without needing a full WebSocket/SSE stack yet.
  readonly trip = signal<Trip | null>(null);

  readonly formatTimestamp = formatTripTimestamp;

  private tripId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tripService: TripService
  ) {}

  ngOnInit(): void {
    this.tripId = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.tripId) {
      this.notFound.set(true);
      this.loading.set(false);
      return;
    }

    // TODO: replace this interval-based poll with a real-time push channel
    // (WebSocket via STOMP, or Server-Sent Events) once the backend exposes
    // one. Polling is a deliberate placeholder - the signal-based `trip()`
    // state below is what a future push implementation would update instead.
    interval(environment.pollingIntervalMs)
      .pipe(
        startWith(0),
        switchMap(() => this.tripService.getTripById(this.tripId)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (trip) => {
          this.trip.set(trip);
          this.loading.set(false);
        },
        error: () => {
          this.notFound.set(true);
          this.loading.set(false);
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/trips']);
  }
}
