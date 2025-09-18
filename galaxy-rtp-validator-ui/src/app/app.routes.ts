import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'generator', loadComponent: () => import('./pages/generator/generator.component').then(m => m.GeneratorComponent) },
  { path: 'enhanced-generator', loadComponent: () => import('./pages/enhanced-generator/enhanced-generator.component').then(m => m.EnhancedGeneratorComponent) },
  { path: 'validator', loadComponent: () => import('./pages/validator/validator.component').then(m => m.ValidatorComponent) },
  { path: 'documentation', loadComponent: () => import('./pages/documentation/documentation.component').then(m => m.DocumentationComponent) },
  { path: '**', redirectTo: '' }
];
