import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { toApiError } from '../core/api-error';
import { EmployeeService } from '../core/employee.service';
import { Employee, EmployeeRequest, FilterOptions } from '../core/models';

export interface EmployeeFormData {
  employee?: Employee;
  options: FilterOptions;
  currencies: string[];
}

/** Convenience only: picking a country pre-selects its usual currency, which HR can still change. */
const USUAL_CURRENCY: Record<string, string> = {
  AU: 'AUD', CA: 'CAD', DE: 'EUR', GB: 'GBP', IN: 'INR', JP: 'JPY', SG: 'SGD', US: 'USD',
};

type FieldName = keyof EmployeeFormDialog['form']['controls'];

const PATTERN_MESSAGES: Partial<Record<FieldName, string>> = {
  employeeCode: 'Use the format EMP-00001',
  countryCode: 'Use a 2-letter country code, e.g. IN',
};

@Component({
  selector: 'app-employee-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './employee-form-dialog.html',
  styleUrl: './employee-form-dialog.scss',
})
export class EmployeeFormDialog {
  protected readonly data = inject<EmployeeFormData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject<MatDialogRef<EmployeeFormDialog, Employee>>(MatDialogRef);
  private readonly employees = inject(EmployeeService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly isEdit = this.data.employee !== undefined;
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly today = new Date().toISOString().slice(0, 10);

  readonly form = inject(NonNullableFormBuilder).group({
    employeeCode: ['', [Validators.required, Validators.pattern(/^\s*[A-Za-z]{3}-\d{5,}\s*$/)]],
    fullName: ['', [Validators.required, Validators.maxLength(200)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    jobTitle: ['', [Validators.required, Validators.maxLength(100)]],
    department: ['', [Validators.required, Validators.maxLength(100)]],
    countryCode: ['', [Validators.required, Validators.pattern(/^\s*[A-Za-z]{2}\s*$/)]],
    currencyCode: ['', Validators.required],
    annualSalary: [null as number | null, [Validators.required, Validators.min(0.01)]],
    hireDate: ['', Validators.required],
  });

  constructor() {
    const employee = this.data.employee;
    if (employee) {
      this.form.patchValue(employee);
    }
    this.form.controls.countryCode.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((country) => this.suggestCurrency(country));
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: EmployeeRequest = {
      ...value,
      annualSalary: value.annualSalary!,
      version: this.data.employee?.version ?? null,
    };
    const save$ = this.data.employee
      ? this.employees.update(this.data.employee.id, request)
      : this.employees.create(request);

    this.saving.set(true);
    this.formError.set(null);
    save$.subscribe({
      next: (saved) => this.dialogRef.close(saved),
      error: (err: unknown) => {
        this.saving.set(false);
        const apiError = toApiError(err);
        this.formError.set(apiError.message);
        for (const [field, message] of Object.entries(apiError.fieldErrors)) {
          const control = this.form.get(field);
          control?.setErrors({ server: message });
          control?.markAsTouched();
        }
      },
    });
  }

  protected errorFor(field: FieldName): string | null {
    const errors = this.form.controls[field].errors;
    if (!errors) return null;
    if (errors['required']) return 'Required';
    if (errors['email']) return 'Enter a valid email address';
    if (errors['pattern']) return PATTERN_MESSAGES[field] ?? 'Invalid format';
    if (errors['min']) return 'Must be greater than 0';
    if (errors['maxlength']) return `Keep it under ${errors['maxlength'].requiredLength} characters`;
    if (errors['server']) return errors['server'];
    return 'Invalid value';
  }

  private suggestCurrency(country: string): void {
    const currency = this.form.controls.currencyCode;
    const usual = USUAL_CURRENCY[country.trim().toUpperCase()];
    if (usual && !currency.dirty && this.data.currencies.includes(usual)) {
      currency.setValue(usual);
    }
  }
}
