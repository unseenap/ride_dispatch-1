import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { TripService } from '../../../core/services/trip.service';
import { Trip, TripStatus } from '../../../core/models/trip.model';
import { TripStatusChipComponent } from '../../shared/components/trip-status-chip.component';
import { formatTripTimestamp } from '../../../core/utils/date-format.util';

const ALL_STATUSES: TripStatus[] = ['REQUESTED', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule,
    TripStatusChipComponent
  ],
  templateUrl: './trip-list.component.html',
  styleUrl: './trip-list.component.scss'
})
export class TripListComponent implements OnInit {
  readonly displayedColumns = ['id', 'rider', 'driver', 'status', 'requestedAt', 'fareEstimate', 'actions'];
  readonly statuses = ALL_STATUSES;
  readonly formatTimestamp = formatTripTimestamp;

  readonly loading = signal(true);
  readonly trips = signal<Trip[]>([]);
  readonly totalElements = signal(0);
  readonly pageSize = signal(10);
  readonly pageIndex = signal(0);
  readonly selectedStatus = signal<TripStatus | null>(null);

  constructor(
    private tripService: TripService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTrips();
  }

  loadTrips(): void {
    this.loading.set(true);
    this.tripService.listTrips(this.pageIndex(), this.pageSize(), this.selectedStatus()).subscribe({
      next: (page) => {
        this.trips.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadTrips();
  }

  onStatusFilterChange(status: TripStatus | null): void {
    this.selectedStatus.set(status);
    this.pageIndex.set(0);
    this.loadTrips();
  }

  viewTrip(trip: Trip): void {
    this.router.navigate(['/trips', trip.id]);
  }
}
