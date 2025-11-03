import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  title = 'PACS.008 Message Generator';

  constructor(private router: Router) {}

  navigateToGenerator(): void {
    this.router.navigate(['/advanced-generator']);
  }

  navigateToProcessor(): void {
    this.router.navigate(['/process-payments']);
  }
}
