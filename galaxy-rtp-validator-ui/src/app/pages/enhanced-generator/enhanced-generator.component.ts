import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { RtpService, EnhancedGeneratorRequest, EnhancedGeneratorResponse, FieldConfiguration, FieldDefinition, FieldGroup } from '../../services/rtp.service';

@Component({
  selector: 'app-enhanced-generator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './enhanced-generator.component.html',
  styleUrls: ['./enhanced-generator.component.css']
})
export class EnhancedGeneratorComponent implements OnInit {
  formData: EnhancedGeneratorRequest = {
    messageType: 'pacs.008',
    numberOfFiles: 5,
    selectedOptionalFields: [],
    selectedConditionalFields: [],
    generateReport: true,
    reportFormat: 'excel',
    testScenario: 'basic',
    validateAgainstXsd: true,
    includeFieldDescriptions: true,
    includeIsoDescriptions: true,
    amountRange: 'medium',
    currency: 'USD',
    businessType: 'BUSINESS',
    serviceLevel: 'SDVA'
  };

  isLoading: boolean = false;
  results: EnhancedGeneratorResponse | null = null;
  selectedMessage: number = 0;
  currentPage: number = 0;
  messagesPerPage: number = 5;

  // Field configuration
  fieldConfiguration: FieldConfiguration | null = null;
  fieldGroups: FieldGroup[] = [];
  mandatoryFields: FieldDefinition[] = [];
  optionalFields: FieldDefinition[] = [];
  conditionalFields: FieldDefinition[] = [];

  // Generation options
  generationOptions: { [key: string]: any } = {};

  // UI state
  showFieldSelection: boolean = false;
  selectedFieldGroup: string = 'all';
  searchTerm: string = '';

  constructor(
    private rtpService: RtpService,
    private toastr: ToastrService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    this.loadFieldConfiguration();
    this.loadGenerationOptions();
  }

  loadFieldConfiguration(): void {
    this.rtpService.getFieldConfiguration().subscribe({
      next: (config) => {
        this.fieldConfiguration = config;
        this.mandatoryFields = config.mandatoryFields;
        this.optionalFields = config.optionalFields;
        this.conditionalFields = config.conditionalFields;
        this.fieldGroups = config.fieldGroups;
        console.log('Field configuration loaded:', config);
      },
      error: (err) => {
        console.error('Error loading field configuration:', err);
        this.toastr.error('Failed to load field configuration', 'Error!');
      }
    });
  }

  loadGenerationOptions(): void {
    this.rtpService.getGenerationOptions().subscribe({
      next: (options) => {
        this.generationOptions = options;
        console.log('Generation options loaded:', options);
      },
      error: (err) => {
        console.error('Error loading generation options:', err);
        this.toastr.error('Failed to load generation options', 'Error!');
      }
    });
  }

  onFieldSelectionChange(fieldPath: string, fieldType: 'optional' | 'conditional', event: Event): void {
    const target = event.target as HTMLInputElement;
    const isSelected = target.checked;
    if (fieldType === 'optional') {
      if (isSelected) {
        if (!this.formData.selectedOptionalFields) {
          this.formData.selectedOptionalFields = [];
        }
        this.formData.selectedOptionalFields.push(fieldPath);
      } else {
        this.formData.selectedOptionalFields = this.formData.selectedOptionalFields?.filter(f => f !== fieldPath) || [];
      }
    } else if (fieldType === 'conditional') {
      if (isSelected) {
        if (!this.formData.selectedConditionalFields) {
          this.formData.selectedConditionalFields = [];
        }
        this.formData.selectedConditionalFields.push(fieldPath);
      } else {
        this.formData.selectedConditionalFields = this.formData.selectedConditionalFields?.filter(f => f !== fieldPath) || [];
      }
    }
  }

  isFieldSelected(fieldPath: string, fieldType: 'optional' | 'conditional'): boolean {
    if (fieldType === 'optional') {
      return this.formData.selectedOptionalFields?.includes(fieldPath) || false;
    } else if (fieldType === 'conditional') {
      return this.formData.selectedConditionalFields?.includes(fieldPath) || false;
    }
    return false;
  }

  getFilteredFields(fieldType: 'optional' | 'conditional'): FieldDefinition[] {
    let fields = fieldType === 'optional' ? this.optionalFields : this.conditionalFields;
    
    // Filter by search term
    if (this.searchTerm) {
      fields = fields.filter(field => 
        field.path.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        field.description.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }
    
    // Filter by field group
    if (this.selectedFieldGroup !== 'all') {
      const group = this.fieldGroups.find(g => g.name === this.selectedFieldGroup);
      if (group) {
        fields = fields.filter(field => group.fields.includes(field.path));
      }
    }
    
    return fields;
  }

  selectAllFields(fieldType: 'optional' | 'conditional'): void {
    const fields = this.getFilteredFields(fieldType);
    if (fieldType === 'optional') {
      this.formData.selectedOptionalFields = fields.map(f => f.path);
    } else {
      this.formData.selectedConditionalFields = fields.map(f => f.path);
    }
  }

  deselectAllFields(fieldType: 'optional' | 'conditional'): void {
    if (fieldType === 'optional') {
      this.formData.selectedOptionalFields = [];
    } else {
      this.formData.selectedConditionalFields = [];
    }
  }

  generateMessages(): void {
    console.log('Starting enhanced message generation...');
    console.log('Request data:', this.formData);
    
    this.isLoading = true;
    
    this.rtpService.generateEnhancedMessages(this.formData).subscribe({
      next: (response: EnhancedGeneratorResponse) => {
        console.log('Enhanced API response received:', response);
        this.isLoading = false;
        
        if (response.success && response.messages && response.messages.length > 0) {
          this.results = response;
          this.selectedMessage = 0;
          console.log('Results set:', this.results);
          
          this.toastr.success(`Successfully generated ${response.messages.length} enhanced ${this.formData.messageType} messages!`, 'Success!');
          
          // Download report if generated
          if (response.reportDownloadUrl) {
            this.downloadReport();
          }
        } else {
          console.error('Failed to generate enhanced messages:', response.message || 'No messages returned');
          this.toastr.error(response.message || 'No messages were generated', 'Error!');
        }
      },
      error: (err) => {
        console.error('Enhanced generation error:', err);
        this.isLoading = false;
        
        let errorMessage = 'Error generating enhanced messages. Please check if the backend server is running on port 8080.';
        
        if (err.error && err.error.error) {
          errorMessage = err.error.error;
        } else if (err.status === 0) {
          errorMessage = 'Cannot connect to backend server. Please ensure the backend is running on port 8080.';
        } else if (err.status === 400) {
          errorMessage = err.error?.error || 'Invalid request. Please check your input.';
        }
        
        this.toastr.error(errorMessage, 'Error!');
      }
    });
  }

  downloadReport(): void {
    if (!this.formData.generateReport) {
      this.toastr.warning('Report generation is disabled', 'Warning!');
      return;
    }

    this.rtpService.generateReport(this.formData).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `RTP_Test_Report_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.toastr.success('Report downloaded successfully!', 'Success!');
      },
      error: (err) => {
        console.error('Error downloading report:', err);
        this.toastr.error('Failed to download report', 'Error!');
      }
    });
  }

  downloadMessage(message: any): void {
    const blob = new Blob([message.xmlContent], { type: 'application/xml' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${message.messageId}.xml`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  copyToClipboard(text: string): void {
    if (isPlatformBrowser(this.platformId)) {
      navigator.clipboard.writeText(text).then(() => {
        this.toastr.success('Copied to clipboard!', 'Success!');
      }).catch(err => {
        console.error('Failed to copy to clipboard:', err);
        this.toastr.error('Failed to copy to clipboard', 'Error!');
      });
    }
  }

  getTotalPages(): number {
    if (!this.results || !this.results.messages) return 0;
    return Math.ceil(this.results.messages.length / this.messagesPerPage);
  }

  getEndIndex(): number {
    if (!this.results || !this.results.messages) return 0;
    return Math.min((this.currentPage + 1) * this.messagesPerPage, this.results.messages.length);
  }

  getCurrentPageMessages(): any[] {
    if (!this.results || !this.results.messages) return [];
    const start = this.currentPage * this.messagesPerPage;
    const end = start + this.messagesPerPage;
    return this.results.messages.slice(start, end);
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.getTotalPages()) {
      this.currentPage = page;
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.getTotalPages() - 1) {
      this.currentPage++;
    }
  }

  clearResults(): void {
    this.results = null;
    this.selectedMessage = 0;
    this.currentPage = 0;
  }

  getFieldDescription(fieldPath: string): string {
    const allFields = [...this.mandatoryFields, ...this.optionalFields, ...this.conditionalFields];
    const field = allFields.find(f => f.path === fieldPath);
    return field ? field.description : 'No description available';
  }

  getFieldIsoDescription(fieldPath: string): string {
    const allFields = [...this.mandatoryFields, ...this.optionalFields, ...this.conditionalFields];
    const field = allFields.find(f => f.path === fieldPath);
    return field ? (field.isoDescription || 'No ISO description available') : 'No ISO description available';
  }
}
