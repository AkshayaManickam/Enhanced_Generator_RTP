import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProcessPaymentRequest {
  pacs008Message: string;
  environment: string;
  tenant: string;
}

export interface ProcessPaymentResponse {
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentProcessingService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  processPayment(request: ProcessPaymentRequest): Observable<ProcessPaymentResponse> {
    return this.http.post<ProcessPaymentResponse>(`${this.apiUrl}/process-payment`, request);
  }
}

