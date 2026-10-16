import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Observable, catchError, combineLatest, of, switchMap } from 'rxjs';
import { toApiError } from '../core/api-error';
import { EmployeeService } from '../core/employee.service';
import { formatCompactUsd, formatDeviation } from '../core/format';
import { InsightsService } from '../core/insights.service';
import { FilterOptions, GroupBy, GroupStats, InsightsFilter, OutlierReport, SalarySummary } from '../core/models';
import { PayRangeBar } from './pay-range-bar';

const GROUP_LABELS: Record<GroupBy, string> = { country: 'Country', department: 'Department', jobTitle: 'Job title' };

@Component({
  selector: 'app-insights-page',
  imports: [CurrencyPipe, DecimalPipe, MatButtonToggleModule, MatFormFieldModule, MatSelectModule, PayRangeBar],
  templateUrl: './insights-page.html',
  styleUrl: './insights-page.scss',
})
export class InsightsPage {
  private readonly insights = inject(InsightsService);
  private readonly employees = inject(EmployeeService);

  protected readonly filter = signal<InsightsFilter>({});
  protected readonly groupBy = signal<GroupBy>('country');
  protected readonly threshold = signal(0.3);

  protected readonly summary = signal<SalarySummary | null>(null);
  protected readonly stats = signal<GroupStats[]>([]);
  protected readonly outliers = signal<OutlierReport | null>(null);
  protected readonly options = signal<FilterOptions>({ countries: [], departments: [], jobTitles: [] });
  protected readonly ratesAsOf = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly groupLabel = computed(() => GROUP_LABELS[this.groupBy()]);
  /** Every bar shares one scale so groups can be compared by eye. */
  protected readonly scaleMax = computed(() => Math.max(0, ...this.stats().map((s) => s.maxUsd)));

  protected readonly formatCompactUsd = formatCompactUsd;
  protected readonly formatDeviation = formatDeviation;

  constructor() {
    const filter$ = toObservable(this.filter);

    filter$
      .pipe(switchMap((f) => this.safely(this.insights.summary(f))), takeUntilDestroyed())
      .subscribe((summary) => this.summary.set(summary));

    combineLatest([filter$, toObservable(this.groupBy)])
      .pipe(switchMap(([f, g]) => this.safely(this.insights.salaryStats(g, f))), takeUntilDestroyed())
      .subscribe((stats) => this.stats.set(stats ?? []));

    combineLatest([filter$, toObservable(this.threshold)])
      .pipe(switchMap(([f, t]) => this.safely(this.insights.outliers(f, t))), takeUntilDestroyed())
      .subscribe((report) => this.outliers.set(report));

    this.employees.filterOptions().subscribe({ next: (o) => this.options.set(o), error: () => {} });
    this.insights.exchangeRates().subscribe({
      next: (rates) => this.ratesAsOf.set(rates[0]?.asOf ?? null),
      error: () => {},
    });
  }

  protected setFilter(changes: Partial<InsightsFilter>): void {
    this.filter.update((f) => ({ ...f, ...changes }));
  }

  private safely<T>(request: Observable<T>): Observable<T | null> {
    this.error.set(null);
    return request.pipe(
      catchError((err: unknown) => {
        this.error.set(toApiError(err).message);
        return of(null);
      }),
    );
  }
}
