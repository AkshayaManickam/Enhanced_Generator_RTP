import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'advanced-generator',
    loadComponent: () => import('./components/advanced-generator/advanced-generator.component').then(m => m.AdvancedGeneratorComponent)
  },
  {
    path: 'process-payments',
    loadComponent: () => import('./components/process-payments/process-payments.component').then(m => m.ProcessPaymentsComponent)
  }
];
