import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats = {
    totalGenerated: 156,
    totalValidated: 89,
    successRate: '94.2%',
    lastActivity: '2 minutes ago'
  };

  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  navigateToGenerator(messageType: string): void {
    this.router.navigate(['/generator'], { queryParams: { type: messageType } });
  }

  navigateToValidator(): void {
    this.router.navigate(['/validator']);
  }

  navigateToDocumentation(): void {
    this.router.navigate(['/documentation']);
  }
}
