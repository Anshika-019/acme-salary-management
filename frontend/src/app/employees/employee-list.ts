import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BehaviorSubject, Subject, catchError, combineLatest, debounceTime, distinctUntilChanged, filter, of, switchMap, tap } from 'rxjs';
import { toApiError } from '../core/api-error';
import { EmployeeService } from '../core/employee.service';
import { InsightsService } from '../core/insights.service';
import { Employee, EmployeeQuery, FilterOptions, PageResponse } from '../core/models';
import { ConfirmDialog, ConfirmDialogData } from '../shared/confirm-dialog';
import { EmployeeFormData, EmployeeFormDialog } from './employee-form-dialog';

const DEFAULT_QUERY: EmployeeQuery = { page: 0, size: 25, sort: 'fullName', direction: 'asc' };

@Component({
  selector: 'app-employee-list',
  imports: [
    CurrencyPipe, MatTableModule, MatSortModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatProgressBarModule, MatTooltipModule,
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeList {
  private readonly employees = inject(EmployeeService);
  private readonly insights = inject(InsightsService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly columns = ['employeeCode', 'fullName', 'jobTitle', 'department', 'countryCode', 'annualSalary', 'annualSalaryUsd', 'actions'];

  protected readonly query = signal<EmployeeQuery>(DEFAULT_QUERY);
  protected readonly page = signal<PageResponse<Employee> | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly options = signal<FilterOptions>({ countries: [], departments: [], jobTitles: [] });
  private readonly currencies = signal<string[]>([]);

  protected readonly hasFilters = computed(() => {
    const q = this.query();
    return Boolean(q.search || q.country || q.department || q.jobTitle);
  });

  private readonly searchTerms = new Subject<string>();
  private readonly refresh = new BehaviorSubject<void>(undefined);

  constructor() {
    this.searchTerms
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((search) => this.updateQuery({ search }));

    // switchMap cancels a slow, stale request when the filters change again.
    combineLatest([toObservable(this.query), this.refresh])
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap(([query]) =>
          this.employees.search(query).pipe(
            catchError((err: unknown) => {
              this.error.set(toApiError(err).message);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((page) => {
        this.loading.set(false);
        if (page) this.page.set(page);
      });

    this.loadFilterOptions();
    this.insights.exchangeRates().subscribe({
      next: (rates) => this.currencies.set(rates.map((r) => r.currencyCode)),
      error: () => this.currencies.set([]),
    });
  }

  protected onSearch(event: Event): void {
    this.searchTerms.next((event.target as HTMLInputElement).value.trim());
  }

  protected onCountryChange(country: string): void {
    // Sorting by salary only makes sense within one currency, so drop it when the country filter is cleared.
    const resetSort = !country && this.query().sort === 'annualSalary';
    this.updateQuery({ country, ...(resetSort ? { sort: 'fullName', direction: 'asc' as const } : {}) });
  }

  protected updateQuery(changes: Partial<EmployeeQuery>): void {
    // Any change other than paging sends the user back to the first page.
    this.query.update((q) => ({ ...q, page: 0, ...changes }));
  }

  protected clearFilters(): void {
    this.query.set(DEFAULT_QUERY);
  }

  protected onSort(sort: Sort): void {
    this.updateQuery(
      sort.direction ? { sort: sort.active, direction: sort.direction } : { sort: DEFAULT_QUERY.sort, direction: 'asc' },
    );
  }

  protected onPage(event: PageEvent): void {
    this.updateQuery({ page: event.pageIndex, size: event.pageSize });
  }

  protected openCreate(): void {
    this.openForm(undefined, 'Employee added');
  }

  protected openEdit(employee: Employee): void {
    this.openForm(employee, 'Changes saved');
  }

  protected confirmDelete(employee: Employee): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        data: {
          title: `Delete ${employee.fullName}?`,
          message: 'This permanently removes the employee and their salary record.',
          confirmLabel: 'Delete employee',
        },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.employees.delete(employee.id)),
      )
      .subscribe({
        next: () => {
          this.snackBar.open('Employee deleted', undefined, { duration: 3000 });
          this.refresh.next();
        },
        error: (err: unknown) => this.snackBar.open(toApiError(err).message, 'Dismiss'),
      });
  }

  private openForm(employee: Employee | undefined, successMessage: string): void {
    this.dialog
      .open<EmployeeFormDialog, EmployeeFormData, Employee>(EmployeeFormDialog, {
        data: { employee, options: this.options(), currencies: this.currencies() },
        width: '640px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => {
        this.snackBar.open(successMessage, undefined, { duration: 3000 });
        this.refresh.next();
        this.loadFilterOptions();
      });
  }

  private loadFilterOptions(): void {
    this.employees.filterOptions().subscribe({
      next: (options) => this.options.set(options),
      error: () => {
        // Filters stay usable as free text even if this fails.
      },
    });
  }
}
