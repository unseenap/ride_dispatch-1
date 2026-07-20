import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TripStatus } from '../../../core/models/trip.model';

@Component({
  selector: 'app-trip-status-chip',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="dh-status-chip" [ngClass]="'dh-status-' + status.toLowerCase()">{{ status.replace('_', ' ') }}</span>`
})
export class TripStatusChipComponent {
  @Input({ required: true }) status!: TripStatus;
}
