import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentInfoRequest, PaymentTemplate } from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentTemplateService {
  private apiUrl = 'http://localhost:8080/api/payment-templates';

  constructor(private http: HttpClient) { }

  saveTemplate(request: PaymentInfoRequest): Observable<PaymentTemplate> {
    return this.http.post<PaymentTemplate>(this.apiUrl, request);
  }

  getAllTemplates(): Observable<PaymentTemplate[]> {
    return this.http.get<PaymentTemplate[]>(this.apiUrl);
  }

  getTemplateById(id: number): Observable<PaymentTemplate> {
    return this.http.get<PaymentTemplate>(`${this.apiUrl}/${id}`);
  }

  updateTemplate(id: number, request: PaymentInfoRequest): Observable<PaymentTemplate> {
    return this.http.put<PaymentTemplate>(`${this.apiUrl}/${id}`, request);
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getTemplateCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count`);
  }
}

