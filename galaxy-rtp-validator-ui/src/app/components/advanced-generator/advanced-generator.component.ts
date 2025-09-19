import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

interface FieldDefinition {
  name: string;
  fieldName: string;
  xpath: string;
  type: 'MANDATORY' | 'OPTIONAL' | 'CONDITIONAL';
  fieldType: 'MANDATORY' | 'OPTIONAL' | 'CONDITIONAL';
  dataType: string;
  maxLength?: number;
  minLength?: number;
  pattern?: string;
  allowedValues?: string[];
  defaultValue?: string;
  description: string;
  category: string;
}

interface FieldCategory {
  [key: string]: FieldDefinition[];
}

interface GenerationRequest {
  selectedFields: string[];
  numberOfMessages: number;
  includeOptionalFields: boolean;
  includeConditionalFields: boolean;
  maxComplexity: number;
  outputDirectory: string;
}

interface GenerationResponse {
  success: boolean;
  errorMessage?: string;
  totalMessages: number;
  filePaths: string[];
  generatedMessages?: GeneratedMessage[];
  statistics: GenerationStatistics;
}

interface GeneratedMessage {
  messageIndex: number;
  combination: FieldCombination;
  document: any;
  xmlContent: string;
  valid: boolean;
  validationErrors: string[];
  generatedAt: Date;
}

interface GenerationStatistics {
  totalMessages: number;
  validMessages: number;
  invalidMessages: number;
  fieldUsageStatistics: { [key: string]: number };
  minComplexity: number;
  maxComplexity: number;
  avgComplexity: number;
  totalCombinations: number;
}

interface FieldCombination {
  combinationId: string;
  includedFields?: string[];
  excludedFields?: string[];
  description: string;
  valid: boolean;
  complexityScore: number;
  priority: number;
  fieldValues: any;
  validationErrors: string[];
  tags: string[];
}

@Component({
  selector: 'app-advanced-generator',
  templateUrl: './advanced-generator.component.html',
  styleUrls: ['./advanced-generator.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class AdvancedGeneratorComponent implements OnInit {
  
  // Field management
  allFields: FieldDefinition[] = [];
  fieldsByCategory: FieldCategory = {};
  selectedFields: Set<string> = new Set();
  mandatoryFields: Set<string> = new Set();
  optionalFields: Set<string> = new Set();
  conditionalFields: Set<string> = new Set();
  
  // Generation settings
  generationSettings = {
    numberOfMessages: 10,
    maxCombinations: 50,
    includeMandatory: true
  };
  numberOfMessages: number = 10;
  includeOptionalFields: boolean = true;
  includeConditionalFields: boolean = true;
  maxComplexity: number = 50;
  outputDirectory: string = 'output';
  
  // UI state
  loading: boolean = false;
  generating: boolean = false;
  isLoading: boolean = false;
  showFieldDetails: boolean = false;
  selectedCategory: string = 'all';
  
  // Results
  generationResult: GenerationResponse | null = null;
  previewCombinationsList: FieldCombination[] = [];
  validationErrors: string[] = [];
  validationWarnings: string[] = [];
  
  // Message viewing
  selectedMessageIndex: number = 0;
  
  // Statistics
  fieldStats = {
    total: 0,
    mandatory: 0,
    optional: 0,
    conditional: 0
  };
  
  constructor(private http: HttpClient) {}
  
  ngOnInit(): void {
    this.loadFieldDefinitions();
  }
  
  /**
   * Load all field definitions from the backend
   */
  loadFieldDefinitions(): void {
    this.loading = true;
    
    this.http.get<any>(`${environment.apiUrl}/api/advanced-generator/fields`)
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.fieldsByCategory = response.fieldsByCategory;
            this.allFields = Object.values(response.allFields);
            
            // Categorize fields
            this.categorizeFields();
            
            // Set statistics
            this.fieldStats = {
              total: response.totalFields,
              mandatory: response.mandatoryFields,
              optional: response.optionalFields,
              conditional: response.conditionalFields
            };
            
            // Auto-select mandatory fields
            this.autoSelectMandatoryFields();
            
            console.log('Field definitions loaded:', this.allFields.length);
          } else {
            console.error('Error loading field definitions:', response.errorMessage);
          }
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading field definitions:', error);
          this.loading = false;
        }
      });
  }
  
  /**
   * Categorize fields by type
   */
  categorizeFields(): void {
    this.mandatoryFields.clear();
    this.optionalFields.clear();
    this.conditionalFields.clear();
    
    this.allFields.forEach(field => {
      switch (field.type || field.fieldType) {
        case 'MANDATORY':
          this.mandatoryFields.add(field.name || field.fieldName);
          break;
        case 'OPTIONAL':
          this.optionalFields.add(field.name || field.fieldName);
          break;
        case 'CONDITIONAL':
          this.conditionalFields.add(field.name || field.fieldName);
          break;
      }
    });
  }
  
  /**
   * Auto-select all mandatory fields
   */
  autoSelectMandatoryFields(): void {
    this.mandatoryFields.forEach(fieldName => {
      this.selectedFields.add(fieldName);
    });
  }
  
  /**
   * Toggle field selection
   */
  toggleFieldSelection(fieldName: string): void {
    if (this.mandatoryFields.has(fieldName)) {
      // Mandatory fields cannot be deselected
      return;
    }
    
    if (this.selectedFields.has(fieldName)) {
      this.selectedFields.delete(fieldName);
    } else {
      this.selectedFields.add(fieldName);
    }
    
    this.validateFieldSelection();
  }
  
  /**
   * Select all fields in a category
   */
  selectCategoryFields(category: string): void {
    if (category === 'all') {
      this.allFields.forEach(field => {
        this.selectedFields.add(field.name || field.fieldName);
      });
    } else {
      const categoryFields = this.fieldsByCategory[category] || [];
      categoryFields.forEach(field => {
        this.selectedFields.add(field.name || field.fieldName);
      });
    }
    
    this.validateFieldSelection();
  }
  
  /**
   * Deselect all fields in a category
   */
  deselectCategoryFields(category: string): void {
    if (category === 'all') {
      this.selectedFields.clear();
      this.autoSelectMandatoryFields();
    } else {
      const categoryFields = this.fieldsByCategory[category] || [];
      categoryFields.forEach(field => {
        const fieldName = field.name || field.fieldName;
        if (!this.mandatoryFields.has(fieldName)) {
          this.selectedFields.delete(fieldName);
        }
      });
    }
    
    this.validateFieldSelection();
  }
  
  /**
   * Validate field selection
   */
  validateFieldSelection(): void {
    const request = {
      selectedFields: Array.from(this.selectedFields)
    };
    
    this.http.post<any>(`${environment.apiUrl}/api/advanced-generator/validate-selection`, request)
      .subscribe({
        next: (response) => {
          this.validationErrors = response.errors || [];
          this.validationWarnings = response.warnings || [];
        },
        error: (error) => {
          console.error('Error validating field selection:', error);
        }
      });
  }
  
  /**
   * Preview field combinations
   */
  previewCombinations(): void {
    const request = {
      selectedFields: Array.from(this.selectedFields),
      maxCombinations: 50
    };
    
    // Use real backend API for preview combinations
    
    this.http.post<any>(`${environment.apiUrl}/api/advanced-generator/combinations/preview`, request)
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.previewCombinationsList = response.previewCombinations || [];
            console.log('Preview combinations loaded:', this.previewCombinationsList.length);
          } else {
            console.error('Error loading preview combinations:', response.errorMessage);
          }
        },
        error: (error) => {
          console.error('Error loading preview combinations:', error);
        }
      });
  }
  
  /**
   * Generate messages
   */
  generateMessages(): void {
    if (this.validationErrors.length > 0) {
      alert('Please fix validation errors before generating messages');
      return;
    }
    
    this.generating = true;
    this.isLoading = true;
    
    // Use real backend API for message generation
    
    const request: GenerationRequest = {
      selectedFields: Array.from(this.selectedFields),
      numberOfMessages: this.numberOfMessages,
      includeOptionalFields: this.includeOptionalFields,
      includeConditionalFields: this.includeConditionalFields,
      maxComplexity: this.maxComplexity,
      outputDirectory: this.outputDirectory
    };
    
    this.http.post<GenerationResponse>(`${environment.apiUrl}/api/advanced-generator/generate`, request)
      .subscribe({
        next: (response) => {
          this.generationResult = response;
          this.generating = false;
          this.isLoading = false;
          
          if (response.success) {
            console.log('Messages generated successfully:', response.totalMessages);
          } else {
            console.error('Error generating messages:', response.errorMessage);
          }
        },
        error: (error) => {
          console.error('Error generating messages:', error);
          this.generating = false;
          this.isLoading = false;
        }
      });
  }
  
  /**
   * Download generated files
   */
  downloadFiles(): void {
    if (!this.generationResult?.filePaths) {
      return;
    }
    
    // Use real backend API for file download
    this.generationResult.filePaths.forEach((filePath, index) => {
      const link = document.createElement('a');
      link.href = `${environment.apiUrl}/api/files/${filePath}`;
      link.download = `pacs.008.${index + 1}.xml`;
      link.click();
    });
    
    console.log('Files downloaded:', this.generationResult.filePaths.length);
  }
  
  /**
   * Get field type badge class
   */
  getFieldTypeClass(fieldType: string): string {
    switch (fieldType) {
      case 'MANDATORY':
        return 'badge-danger';
      case 'OPTIONAL':
        return 'badge-primary';
      case 'CONDITIONAL':
        return 'badge-warning';
      default:
        return 'badge-secondary';
    }
  }
  
  /**
   * Get field type display name
   */
  getFieldTypeDisplay(fieldType: string): string {
    switch (fieldType) {
      case 'MANDATORY':
        return 'M';
      case 'OPTIONAL':
        return 'O';
      case 'CONDITIONAL':
        return 'C';
      default:
        return '?';
    }
  }
  
  /**
   * Check if field is selected
   */
  isFieldSelected(fieldName: string): boolean {
    return this.selectedFields.has(fieldName);
  }
  
  /**
   * Check if field is mandatory
   */
  isFieldMandatory(fieldName: string): boolean {
    return this.mandatoryFields.has(fieldName);
  }
  
  /**
   * Get fields for current category
   */
  getCurrentCategoryFields(): FieldDefinition[] {
    if (this.selectedCategory === 'all') {
      return this.allFields;
    }
    return this.fieldsByCategory[this.selectedCategory] || [];
  }
  
  /**
   * Get category names
   */
  getCategoryNames(): string[] {
    return ['all', ...Object.keys(this.fieldsByCategory)];
  }
  
  /**
   * Get selected field count by type
   */
  getSelectedFieldCounts() {
    return {
      mandatory: Array.from(this.selectedFields).filter(f => this.mandatoryFields.has(f)).length,
      optional: Array.from(this.selectedFields).filter(f => this.optionalFields.has(f)).length,
      conditional: Array.from(this.selectedFields).filter(f => this.conditionalFields.has(f)).length,
      total: this.selectedFields.size
    };
  }

  /**
   * Get mandatory field count
   */
  getMandatoryCount(): number {
    return this.mandatoryFields.size;
  }

  /**
   * Get optional field count
   */
  getOptionalCount(): number {
    return this.optionalFields.size;
  }

  /**
   * Get conditional field count
   */
  getConditionalCount(): number {
    return this.conditionalFields.size;
  }

  /**
   * Get filtered fields based on selected category
   */
  getFilteredFields(): FieldDefinition[] {
    if (this.selectedCategory === 'all') {
      return this.allFields;
    }
    return this.fieldsByCategory[this.selectedCategory] || [];
  }

  /**
   * Toggle field selection (overloaded for FieldDefinition)
   */
  toggleFieldSelectionField(field: FieldDefinition): void {
    this.toggleFieldSelection(field.name || field.fieldName);
  }

  /**
   * Select all fields
   */
  selectAllFields(): void {
    this.allFields.forEach(field => {
      this.selectedFields.add(field.name || field.fieldName);
    });
  }

  /**
   * Clear all field selections (except mandatory)
   */
  clearSelection(): void {
    this.selectedFields.clear();
    this.autoSelectMandatoryFields();
  }

  /**
   * Get formatted date
   */
  getFormattedDate(): string {
    return new Date().toLocaleString();
  }

  getIncludedFields(combination: FieldCombination): string[] {
    return combination.includedFields || [];
  }

  getIncludedFieldsCount(combination: FieldCombination): number {
    return combination.includedFields ? combination.includedFields.length : 0;
  }

  /**
   * Message navigation methods
   */
  previousMessage(): void {
    if (this.selectedMessageIndex > 0) {
      this.selectedMessageIndex--;
    }
  }

  nextMessage(): void {
    if (this.generationResult?.generatedMessages && 
        this.selectedMessageIndex < this.generationResult.generatedMessages.length - 1) {
      this.selectedMessageIndex++;
    }
  }

  /**
   * Get current message
   */
  getCurrentMessage(): any {
    if (this.generationResult?.generatedMessages && 
        this.generationResult.generatedMessages.length > 0) {
      return this.generationResult.generatedMessages[this.selectedMessageIndex];
    }
    return null;
  }

  /**
   * Get current message XML content
   */
  getCurrentMessageXml(): string {
    const currentMessage = this.getCurrentMessage();
    return currentMessage?.xmlContent || '';
  }

  /**
   * Copy current message to clipboard
   */
  copyMessageToClipboard(): void {
    const xmlContent = this.getCurrentMessageXml();
    if (xmlContent) {
      navigator.clipboard.writeText(xmlContent).then(() => {
        // You could add a toast notification here
        console.log('Message copied to clipboard');
      }).catch(err => {
        console.error('Failed to copy message: ', err);
      });
    }
  }

  /**
   * Download current message
   */
  downloadCurrentMessage(): void {
    const currentMessage = this.getCurrentMessage();
    if (currentMessage?.xmlContent) {
      const blob = new Blob([currentMessage.xmlContent], { type: 'application/xml' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `pacs.008.${this.selectedMessageIndex + 1}.xml`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  }

  /**
   * Download all messages
   */
  downloadAllMessages(): void {
    if (this.generationResult?.generatedMessages) {
      this.generationResult.generatedMessages.forEach((message, index) => {
        if (message.xmlContent) {
          const blob = new Blob([message.xmlContent], { type: 'application/xml' });
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `pacs.008.${index + 1}.xml`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        }
      });
    }
  }

}
