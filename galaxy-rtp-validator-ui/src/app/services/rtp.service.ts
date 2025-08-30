import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GenerateRtpRequest {
  numberOfFiles: number;
  messageType: string;
}

export interface ValidateRtpRequest {
  xmlContent: string;
  messageType?: string;
}

export interface ValidationResult {
  isValid: boolean;
  messageType?: string;
  schemaErrors?: string[];
  businessRuleErrors?: string[];
  warnings?: Array<{
    message: string;
    location?: string;
  }>;
}

export interface ValidationResultItem {
  messageId: string;
  isValid: boolean;
  validationSummary: string;
  validationResult: ValidationResult;
}

export interface RtpResponse {
  success: boolean;
  message?: string;
  messages?: string[];
  validationResults?: ValidationResultItem[];
  validationResult?: ValidationResult;
  errors?: string[];
  metadata?: any;
}

@Injectable({
  providedIn: 'root'
})
export class RtpService {
  private apiUrl = '/rtp-message';

  constructor(private http: HttpClient) { }

  generateRtpMessages(request: GenerateRtpRequest): Observable<RtpResponse> {
    return this.http.post<RtpResponse>(`${this.apiUrl}/rtp/messages`, request);
  }

  validateRtpMessage(request: ValidateRtpRequest): Observable<RtpResponse> {
    return this.http.post<RtpResponse>(`${this.apiUrl}/rtp/validate`, request);
  }

  getHealth(): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/actuator/health`);
  }

  getSupportedMessageTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/rtp/supported-messages`);
  }
}
