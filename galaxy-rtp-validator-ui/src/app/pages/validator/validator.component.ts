import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { RtpService, ValidateRtpRequest, RtpResponse, GenerateRtpRequest } from '../../services/rtp.service';

@Component({
  selector: 'app-validator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './validator.component.html',
  styleUrls: ['./validator.component.css']
})
export class ValidatorComponent {
  xmlContent: string = '';
  messageType: string = 'pacs.008';
  isLoading: boolean = false;
  validationResult: any = null;

  messageTypes = [
    { value: 'pacs.008', label: 'PACS.008 - Customer Credit Transfer' }
  ];

  constructor(
    private rtpService: RtpService,
    private toastr: ToastrService
  ) { }

  validateMessage(): void {
    if (!this.xmlContent.trim()) {
      this.toastr.error('Please enter XML content to validate', 'Error!');
      return;
    }

    console.log('Starting message validation...');
    this.isLoading = true;

    const request: ValidateRtpRequest = {
      xmlContent: this.xmlContent,
      messageType: this.messageType
    };

    console.log('API request:', request);

    this.rtpService.validateRtpMessage(request).subscribe({
      next: (response: RtpResponse) => {
        console.log('API response received:', response);
        this.isLoading = false;
        this.validationResult = response;
        
        if (response.success) {
          this.toastr.success('Message validation completed!', 'Success!');
        } else {
          this.toastr.warning('Message validation completed with issues', 'Warning!');
        }
      },
      error: (err) => {
        console.error('Validation error:', err);
        this.isLoading = false;
        this.toastr.error('Error validating message. Please check if the backend server is running on port 8080.', 'Error!');
      }
    });
  }

  clearContent(): void {
    this.xmlContent = '';
    this.validationResult = null;
  }

  formData = {
    messageType: 'pacs.008',
    numberOfFiles: 5
  };
  results: any = null;
  loadSampleMessage(): void {
    this.xmlContent = ""; 
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
          this.xmlContent=response.messages[0];
          console.log('Results set:', this.xmlContent);
          this.toastr.success('Valid sample message loaded!', 'Success!');
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

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.xmlContent = e.target.result;
        this.toastr.success('File uploaded successfully!', 'Success!');
      };
      reader.readAsText(file);
    }
  }

  copyXml(): void {
    if (this.xmlContent) {
      navigator.clipboard.writeText(this.xmlContent).then(() => {
        this.toastr.success('XML content copied to clipboard!', 'Success!');
      }).catch(() => {
        this.toastr.error('Failed to copy to clipboard', 'Error!');
      });
    }
  }

  clearResults(): void {
    this.validationResult = null;
  }


}
