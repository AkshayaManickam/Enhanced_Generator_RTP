import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-process-payments',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './process-payments.component.html',
  styleUrl: './process-payments.component.scss'
})
export class ProcessPaymentsComponent {
  constructor(private router: Router) {}

  backToHome(): void {
    this.router.navigate(['/home']);
  }
}
