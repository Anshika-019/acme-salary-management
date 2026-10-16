// TypeScript mirrors of the backend API contracts.

export interface Employee {
  id: number;
  employeeCode: string;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  currencyCode: string;
  annualSalary: number;
  annualSalaryUsd: number | null;
  hireDate: string;
  version: number;
}

export interface EmployeeRequest {
  employeeCode: string;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  currencyCode: string;
  annualSalary: number;
  hireDate: string;
  version: number | null;
}

export interface EmployeeQuery {
  search?: string;
  country?: string;
  department?: string;
  jobTitle?: string;
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface FilterOptions {
  countries: string[];
  departments: string[];
  jobTitles: string[];
}

export interface ExchangeRate {
  currencyCode: string;
  rateToUsd: number;
  asOf: string;
}

export type GroupBy = 'country' | 'department' | 'jobTitle';

export interface InsightsFilter {
  country?: string;
  department?: string;
}

export interface SalarySummary {
  headcount: number;
  totalPayrollUsd: number;
  medianUsd: number | null;
  averageUsd: number | null;
  countries: number;
  departments: number;
}

export interface GroupStats {
  group: string;
  headcount: number;
  minUsd: number;
  p25Usd: number;
  medianUsd: number;
  averageUsd: number;
  p75Usd: number;
  maxUsd: number;
  localCurrency: string | null;
  medianLocal: number | null;
}

export interface Outlier {
  id: number;
  employeeCode: string;
  fullName: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  currencyCode: string;
  annualSalary: number;
  peerMedian: number;
  peerCount: number;
  deviationPercent: number;
}

export interface OutlierReport {
  threshold: number;
  minPeerGroupSize: number;
  totalCount: number;
  outliers: Outlier[];
}
