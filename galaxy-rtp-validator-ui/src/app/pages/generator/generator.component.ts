import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import JSZip from 'jszip';
import { RtpService, GenerateRtpRequest, RtpResponse } from '../../services/rtp.service';

@Component({
  selector: 'app-generator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './generator.component.html',
  styleUrls: ['./generator.component.css']
})
export class GeneratorComponent implements OnInit {
  formData = {
    messageType: 'pacs.008',
    typeOfPayments: 'RTP',
    numberOfFiles: 5,
    optionalTags: 'Default'
  };

  isLoading: boolean = false;
  results: any = null;
  selectedMessage: number = 0;
  isInputExceeded: boolean = false;
  currentPage: number = 0;
  messagesPerPage: number = 5;


  messageTypes = [
    { value: 'pacs.008', label: 'PACS.008 - Customer Credit Transfer', description: 'Credit transfer messages for customer payments' }
  ];

   typeOfPayments = [
    { value: 'RTP', label: 'PACS.008 - Customer Credit Transfer', description: 'Credit transfer messages for customer payments' }
  ];

  // generator.component.ts
  optionalTags = ['Default', 'PmtId', 'PstlAdr', 'DbtrAgt'];

  constructor(
    private rtpService: RtpService,
    private toastr: ToastrService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    // Check for query parameters to pre-select message type (only in browser)
    if (isPlatformBrowser(this.platformId)) {
      const urlParams = new URLSearchParams(window.location.search);
      const typeParam = urlParams.get('type');
      if (typeParam && ['pacs.008'].includes(typeParam)) {
        this.formData.messageType = typeParam;
      }
    }
  }

  getSelectedMessageTypeDescription(): string {
    const selectedType = this.messageTypes.find(t => t.value === this.formData.messageType);
    return selectedType ? selectedType.description : '';
  }

  getFormattedDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  handleInputChange(event: any): void {
    const { name, value, type, checked } = event.target;
    
    // Validate numberOfFiles input
    if (name === 'numberOfFiles') {
      const numValue = parseInt(value);
      if (numValue > 1000) {
        // Set flag to disable generate button
        this.isInputExceeded = true;
        this.formData = {
          ...this.formData,
          [name]: value
        };
        this.toastr.warning('Maximum limit is 1000 files. Generate button will be disabled.', 'Limit Exceeded');
        return;
      } else {
        // Reset flag if value is within limit
        this.isInputExceeded = false;
      }
    }
    
    this.formData = {
      ...this.formData,
      [name]: type === 'checkbox' ? checked : value
    };
  }

  generateMessages(): void {
    console.log('Starting message generation...');
    console.log('Request data:', this.formData);
    
    this.isLoading = true;
    
    const request: GenerateRtpRequest = {
      messageType: this.formData.messageType,
      numberOfFiles: this.formData.numberOfFiles
    };

    console.log('API request:', request);

    this.rtpService.generateRtpMessages(request).subscribe({
      next: (response: RtpResponse) => {
        console.log('API response received:', response);
        this.isLoading = false;
        
        if (response.success && response.messages && response.messages.length > 0) {
          this.results = {
            messages: response.messages,
            metadata: response.metadata || {
              messageType: this.formData.messageType,
              generatedAt: new Date().toISOString()
            }
          };
          this.selectedMessage = 0;
          console.log('Results set:', this.results);
          
          this.toastr.success(`Successfully generated ${response.messages.length} ${this.formData.messageType} messages!`, 'Success!');
        } else {
          console.error('Failed to generate messages:', response.message || 'No messages returned');
          this.toastr.error(response.message || 'No messages were generated', 'Error!');
        }
      },
      error: (err) => {
        console.error('Generation error:', err);
        this.isLoading = false;
        
        let errorMessage = 'Error generating messages. Please check if the backend server is running on port 8080.';
        
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

  downloadMessage(message: string, index: number): void {
    const blob = new Blob([message], { type: 'application/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${this.formData.messageType}_sample_test_case_${index + 1}.xml`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    this.toastr.success('Sample test case downloaded successfully!', 'Download Complete!');
  }

  copyToClipboard(message: string): void {
    navigator.clipboard.writeText(message).then(() => {
      this.toastr.success('Message copied to clipboard!', 'Copied!');
    }).catch(err => {
      console.error('Failed to copy: ', err);
      this.toastr.error('Failed to copy message to clipboard', 'Error!');
    });
  }

  async downloadAllMessages(): Promise<void> {
    if (!this.results?.messages) return;
    
    try {
      const zip = new JSZip();
      
      this.results.messages.forEach((message: string, index: number) => {
        zip.file(`${this.formData.messageType}_sample_test_case_${index + 1}.xml`, message);
      });
      
      const content = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(content);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${this.formData.messageType}_messages.zip`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      
      this.toastr.success('All messages downloaded as ZIP!', 'Download Complete!');
    } catch (error) {
      console.error('Error creating ZIP:', error);
      this.toastr.error('Failed to create ZIP file', 'Error!');
    }
  }

  selectMessage(index: number): void {
    this.selectedMessage = index;
  }

  getPaginatedMessages(): any[] {
    if (!this.results?.messages) return [];
    const startIndex = this.currentPage * this.messagesPerPage;
    const endIndex = startIndex + this.messagesPerPage;
    return this.results.messages.slice(startIndex, endIndex);
  }

  getTotalPages(): number {
    if (!this.results?.messages) return 0;
    return Math.ceil(this.results.messages.length / this.messagesPerPage);
  }

  getVisiblePages(): number[] {
    const totalPages = this.getTotalPages();
    if (totalPages <= 7) {
      // If 7 or fewer pages, show all
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    const pages: number[] = [];
    const maxVisible = 5; // Show max 5 page buttons
    
    if (this.currentPage <= 2) {
      // Near the beginning: show pages 1-5
      for (let i = 0; i < Math.min(maxVisible, totalPages); i++) {
        pages.push(i);
      }
    } else if (this.currentPage >= totalPages - 3) {
      // Near the end: show last 5 pages
      for (let i = Math.max(0, totalPages - maxVisible); i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      // In the middle: show current page ± 2
      for (let i = this.currentPage - 2; i <= this.currentPage + 2; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.getTotalPages()) {
      this.currentPage = page;
      // Reset selected message to first message of current page
      this.selectedMessage = this.currentPage * this.messagesPerPage;
    }
  }

  getMessageIndex(displayIndex: number): number {
    return this.currentPage * this.messagesPerPage + displayIndex;
  }
}
