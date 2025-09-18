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

export interface FieldDefinition {
  path: string;
  description: string;
  dataType: string;
  sampleValue: string;
  isoDescription?: string;
  condition?: string;
}

export interface FieldGroup {
  name: string;
  description: string;
  fields: string[];
}

export interface FieldConfiguration {
  mandatoryFields: FieldDefinition[];
  optionalFields: FieldDefinition[];
  conditionalFields: FieldDefinition[];
  fieldGroups: FieldGroup[];
}

export interface EnhancedGeneratorRequest {
  messageType: string;
  numberOfFiles: number;
  selectedOptionalFields?: string[];
  selectedConditionalFields?: string[];
  customFieldValues?: { [key: string]: any };
  generateReport?: boolean;
  reportFormat?: string;
  testScenario?: string;
  validateAgainstXsd?: boolean;
  includeFieldDescriptions?: boolean;
  includeIsoDescriptions?: boolean;
  amountRange?: string;
  currency?: string;
  businessType?: string;
  serviceLevel?: string;
}

export interface GeneratedMessage {
  messageId: string;
  xmlContent: string;
  includedFields: string[];
  excludedFields: string[];
  fieldDescriptions: { [key: string]: string };
  isValid: boolean;
  validationErrors: string[];
  testScenario: string;
  fieldValues: { [key: string]: any };
}

export interface GenerationMetadata {
  messageType: string;
  totalMessagesGenerated: number;
  numberOfFiles: number;
  generatedAt: string;
  testScenario: string;
  selectedOptionalFields?: string[];
  selectedConditionalFields?: string[];
  generationOptions: { [key: string]: any };
  reportFormat?: string;
  reportGenerated: boolean;
}

export interface EnhancedGeneratorResponse {
  success: boolean;
  message: string;
  messages: GeneratedMessage[];
  metadata: GenerationMetadata;
  reportDownloadUrl?: string;
  errors?: string[];
  warnings?: string[];
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

  // Enhanced generator methods
  generateEnhancedMessages(request: EnhancedGeneratorRequest): Observable<EnhancedGeneratorResponse> {
    return this.http.post<EnhancedGeneratorResponse>(`${this.apiUrl}/api/rtp/generate-enhanced`, request);
  }

  getFieldConfiguration(): Observable<FieldConfiguration> {
    return this.http.get<FieldConfiguration>(`${this.apiUrl}/api/rtp/field-configuration`);
  }

  getFieldGroups(): Observable<FieldGroup[]> {
    return this.http.get<FieldGroup[]>(`${this.apiUrl}/api/rtp/field-groups`);
  }

  getMandatoryFields(): Observable<FieldDefinition[]> {
    return this.http.get<FieldDefinition[]>(`${this.apiUrl}/api/rtp/mandatory-fields`);
  }

  getOptionalFields(): Observable<FieldDefinition[]> {
    return this.http.get<FieldDefinition[]>(`${this.apiUrl}/api/rtp/optional-fields`);
  }

  getConditionalFields(): Observable<FieldDefinition[]> {
    return this.http.get<FieldDefinition[]>(`${this.apiUrl}/api/rtp/conditional-fields`);
  }

  generateReport(request: EnhancedGeneratorRequest): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/api/rtp/generate-report`, request, {
      responseType: 'blob'
    });
  }

  getGenerationOptions(): Observable<{ [key: string]: any }> {
    return this.http.get<{ [key: string]: any }>(`${this.apiUrl}/api/rtp/generation-options`);
  }
}
