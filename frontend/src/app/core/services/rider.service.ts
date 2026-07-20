import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RiderProfile } from '../models/rider.model';

@Injectable({ providedIn: 'root' })
export class RiderService {
  private readonly baseUrl = `${environment.apiBaseUrl}/riders`;

  constructor(private http: HttpClient) {}

  getMyRiderProfile(): Observable<RiderProfile> {
    return this.http.get<RiderProfile>(`${this.baseUrl}/me`);
  }
}
