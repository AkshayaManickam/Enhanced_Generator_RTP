import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PaymentProcessingService, ProcessPaymentRequest } from '../../services/payment-processing.service';

@Component({
  selector: 'app-process-payments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './process-payments.component.html',
  styleUrl: './process-payments.component.scss'
})
export class ProcessPaymentsComponent implements OnInit {
  // Payment Processing
  pacs008Message: string = '';
  messageType: string = 'PACS.008 Message';
  environment: string = 'Dev';
  tenant: string = 'banka';
  isProcessing: boolean = false;

  // Messages
  message: string = '';
  messageCategory: 'success' | 'error' | 'info' = 'info';

  constructor(
    private router: Router,
    private paymentProcessingService: PaymentProcessingService
  ) {}

  ngOnInit(): void {
    // No initialization needed
  }

  validateForm(): boolean {
    return !!(
      this.pacs008Message &&
      this.pacs008Message.trim().length > 0 &&
      this.environment &&
      this.tenant
    );
  }

  // Payment Processing
  processPayment(): void {
    if (!this.validateForm()) {
      this.showMessage('Please provide a PACS.008 message and select environment and tenant', 'error');
      return;
    }

    this.isProcessing = true;
    this.showMessage('Processing payment...', 'info');

    const request: ProcessPaymentRequest = {
      pacs008Message: this.pacs008Message,
      environment: this.environment,
      tenant: this.tenant
    };

    this.paymentProcessingService.processPayment(request).subscribe({
      next: (response) => {
        this.isProcessing = false;
        if (response.success) {
          this.showMessage(response.message || 'Payment processed successfully!', 'success');
          
          setTimeout(() => {
            if (confirm('Payment processed successfully! Would you like to process another payment?')) {
              this.resetForm();
            }
          }, 2000);
        } else {
          this.showMessage(response.message || 'Payment processing failed', 'error');
        }
      },
      error: (error) => {
        this.isProcessing = false;
        console.error('Error processing payment:', error);
        
        let errorMessage = 'Failed to process payment. Please try again.';
        if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }
        
        this.showMessage(errorMessage, 'error');
      }
    });
  }

  resetForm(): void {
    this.pacs008Message = '';
    this.environment = 'Dev';
    this.tenant = 'banka';
    this.messageType = 'PACS.008 Message';
    this.message = '';
  }

  pasteFromClipboard(): void {
    navigator.clipboard.readText().then(text => {
      this.pacs008Message = text;
      this.showMessage('Message pasted from clipboard', 'success');
    }).catch(err => {
      console.error('Failed to read clipboard:', err);
      this.showMessage('Failed to read clipboard. Please paste manually.', 'error');
    });
  }

  clearMessage(): void {
    if (confirm('Are you sure you want to clear the message?')) {
      this.pacs008Message = '';
      this.showMessage('Message cleared', 'info');
    }
  }

  showMessage(message: string, type: 'success' | 'error' | 'info'): void {
    this.message = message;
    this.messageCategory = type;
    setTimeout(() => {
      if (type !== 'error') {
        this.message = '';
      }
    }, 5000);
  }

  backToHome(): void {
    this.router.navigate(['/home']);
  }
}
