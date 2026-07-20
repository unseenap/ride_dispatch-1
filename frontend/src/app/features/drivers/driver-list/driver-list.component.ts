import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

import { DriverService } from '../../../core/services/driver.service';
import { DriverProfile, DriverStatus } from '../../../core/models/driver.model';

const ALL_STATUSES: DriverStatus[] = ['OFFLINE', 'AVAILABLE', 'ON_TRIP'];

@Component({
  selector: 'app-driver-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './driver-list.component.html',
  styleUrl: './driver-list.component.scss'
})
export class DriverListComponent implements OnInit {
  readonly displayedColumns = ['name', 'vehicle', 'plate', 'status', 'rating', 'trips'];
  readonly statuses = ALL_STATUSES;

  // NOTE: no loading signal here - the table renders empty until the first
  // response arrives, with nothing shown in between.
  readonly drivers = signal<DriverProfile[]>([]);
  readonly totalElements = signal(0);
  readonly pageSize = signal(10);
  readonly pageIndex = signal(0);
  readonly selectedStatus = signal<DriverStatus | null>(null);

  constructor(private driverService: DriverService) {}

  ngOnInit(): void {
    this.loadDrivers();
  }

  loadDrivers(): void {
    this.driverService.listDrivers(this.pageIndex(), this.pageSize(), this.selectedStatus()).subscribe((page) => {
      this.drivers.set(page.content);
      this.totalElements.set(page.totalElements);
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadDrivers();
  }

  onStatusFilterChange(status: DriverStatus | null): void {
    this.selectedStatus.set(status);
    this.pageIndex.set(0);
    this.loadDrivers();
  }
}
