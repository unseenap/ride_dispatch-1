import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

/**
 * TODO: rider-facing trip history page. Should call
 * TripService.getMyTrips(page, size, status) - already implemented in
 * TripService and backed by GET /api/trips/my - and render it as a
 * paginated list (see TripListComponent for a reference pattern), plus a
 * link into TripDetailComponent for each row.
 *
 * This is currently just a placeholder shell wired into the router so the
 * "My trip history" nav link doesn't 404.
 */
@Component({
  selector: 'app-trip-history',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <div class="dh-page">
      <div class="dh-page-header">
        <h1>My trip history</h1>
      </div>
      <mat-card class="dh-empty-state">
        <mat-icon>construction</mat-icon>
        <p>Trip history is coming soon.</p>
      </mat-card>
    </div>
  `
})
export class TripHistoryComponent {}
