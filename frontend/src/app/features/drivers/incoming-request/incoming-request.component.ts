import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

/**
 * TODO: driver-facing "incoming trip request" page.
 *
 * Intended behavior: poll (or eventually subscribe via WebSocket/SSE, same
 * TODO as TripDetailComponent) for trips in REQUESTED status near the
 * driver's current location, surface them here as actionable cards, and let
 * the driver Accept (POST /api/trips/{id}/accept) or ignore/let it expire.
 * Needs the driver to be AVAILABLE (DriverService.updateAvailability) before
 * anything will show up.
 *
 * This is currently a static placeholder wired into the router under
 * /incoming-requests so the driver nav link has somewhere to go.
 */
@Component({
  selector: 'app-incoming-request',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule],
  template: `
    <div class="dh-page">
      <div class="dh-page-header">
        <h1>Incoming requests</h1>
      </div>
      <mat-card class="dh-empty-state">
        <mat-icon>notifications_active</mat-icon>
        <p>Live trip requests will appear here once you're online.</p>
        <p class="hint">This screen is a placeholder - see the TODO in incoming-request.component.ts.</p>
      </mat-card>
    </div>
  `,
  styles: [`
    .hint {
      font-size: 0.8rem;
      color: rgba(0, 0, 0, 0.45);
    }
  `]
})
export class IncomingRequestComponent {}
