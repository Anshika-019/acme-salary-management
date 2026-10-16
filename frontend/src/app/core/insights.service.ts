import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { toHttpParams } from './http-params';
import { ExchangeRate, GroupBy, GroupStats, InsightsFilter, OutlierReport, SalarySummary } from './models';

@Injectable({ providedIn: 'root' })
export class InsightsService {
  private readonly http = inject(HttpClient);

  summary(filter: InsightsFilter): Observable<SalarySummary> {
    return this.http.get<SalarySummary>('/api/insights/summary', { params: toHttpParams(filter) });
  }

  salaryStats(groupBy: GroupBy, filter: InsightsFilter): Observable<GroupStats[]> {
    return this.http.get<GroupStats[]>('/api/insights/salary-stats', {
      params: toHttpParams({ groupBy, ...filter }),
    });
  }

  outliers(filter: InsightsFilter, threshold: number): Observable<OutlierReport> {
    return this.http.get<OutlierReport>('/api/insights/outliers', {
      params: toHttpParams({ threshold, ...filter }),
    });
  }

  exchangeRates(): Observable<ExchangeRate[]> {
    return this.http.get<ExchangeRate[]>('/api/exchange-rates');
  }
}
