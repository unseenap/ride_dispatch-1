import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Older card-style renderer for a trip, from before TripListComponent moved
 * to a Material table. Not imported or routed anywhere anymore - left over
 * from the table migration.
 */
@Component({
  selector: 'app-legacy-trip-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="legacy-card">
      <strong>Trip #{{ tripId }}</strong>
      <span>{{ status }}</span>
    </div>
  `,
  styles: [`
    .legacy-card {
      border: 1px solid #ccc;
      padding: 12px;
      margin-bottom: 8px;
      display: flex;
      justify-content: space-between;
    }
  `]
})
export class LegacyTripCardComponent {
  @Input() tripId!: number;
  @Input() status!: string;
}
