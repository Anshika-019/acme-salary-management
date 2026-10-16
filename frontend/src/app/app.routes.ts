import { Routes } from '@angular/router';

// Pages are lazy-loaded so the first screen downloads only what it needs.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · ACME Pay',
    loadComponent: () => import('./employees/employee-list').then((m) => m.EmployeeList),
  },
  {
    path: 'insights',
    title: 'Pay insights · ACME Pay',
    loadComponent: () => import('./insights/insights-page').then((m) => m.InsightsPage),
  },
  { path: '**', redirectTo: 'employees' },
];
