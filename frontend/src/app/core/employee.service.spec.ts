import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EmployeeService } from './employee.service';
import { EmployeeRequest } from './models';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(EmployeeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the filters that are set', () => {
    service.search({ search: 'asha', country: '', page: 2, size: 25, sort: 'fullName', direction: 'asc' }).subscribe();

    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.get('search')).toBe('asha');
    expect(req.request.params.has('country')).toBe(false);
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ content: [], page: 2, size: 25, totalElements: 0, totalPages: 0 });
  });

  it('updates an employee with PUT including its version', () => {
    const request: EmployeeRequest = {
      employeeCode: 'EMP-00042', fullName: 'Asha Verma', email: 'asha@acme.com', jobTitle: 'Software Engineer',
      department: 'Engineering', countryCode: 'IN', currencyCode: 'INR', annualSalary: 1_500_000,
      hireDate: '2022-04-01', version: 3,
    };

    service.update(42, request).subscribe();

    const req = http.expectOne('/api/employees/42');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.version).toBe(3);
    req.flush({});
  });
});
