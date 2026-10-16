package com.acme.salary.employee;

import com.acme.salary.common.BadRequestException;
import com.acme.salary.common.ConflictException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.common.PageResponse;
import com.acme.salary.currency.ExchangeRate;
import com.acme.salary.currency.ExchangeRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employees;
    private final ExchangeRateRepository rates;

    public EmployeeService(EmployeeRepository employees, ExchangeRateRepository rates) {
        this.employees = employees;
        this.rates = rates;
    }

    public PageResponse<EmployeeResponse> search(EmployeeFilter filter, int page, int size,
                                                 EmployeeSortField sortField, Sort.Direction direction) {
        // Secondary sort on id keeps ordering stable across pages when the primary values tie.
        Sort sort = Sort.by(direction, sortField.property()).and(Sort.by("id"));
        Page<Employee> result = employees.findAll(EmployeeSpecifications.matching(filter), PageRequest.of(page, size, sort));

        Map<String, ExchangeRate> rateByCurrency = ratesByCurrency();
        return PageResponse.of(result.map(e -> EmployeeResponse.from(e, rateByCurrency.get(e.getCurrencyCode()))));
    }

    public EmployeeResponse get(long id) {
        Employee employee = findOrThrow(id);
        return EmployeeResponse.from(employee, rates.findById(employee.getCurrencyCode()).orElse(null));
    }

    public FilterOptions filterOptions() {
        return new FilterOptions(
                employees.findDistinctCountryCodes(),
                employees.findDistinctDepartments(),
                employees.findDistinctJobTitles());
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        EmployeeRequest r = request.normalized();
        ExchangeRate rate = requireKnownCurrency(r.currencyCode());
        if (employees.existsByEmployeeCode(r.employeeCode())) {
            throw new ConflictException("Employee code " + r.employeeCode() + " is already in use.");
        }
        if (employees.existsByEmail(r.email())) {
            throw new ConflictException("Email " + r.email() + " is already in use.");
        }

        Employee saved = employees.saveAndFlush(new Employee(r.employeeCode(), r.fullName(), r.email(),
                r.jobTitle(), r.department(), r.countryCode(), r.currencyCode(), r.annualSalary(), r.hireDate()));
        return EmployeeResponse.from(saved, rate);
    }

    @Transactional
    public EmployeeResponse update(long id, EmployeeRequest request) {
        EmployeeRequest r = request.normalized();
        if (r.version() == null) {
            throw new BadRequestException("version is required when updating an employee.");
        }
        Employee employee = findOrThrow(id);
        if (!Objects.equals(employee.getVersion(), r.version())) {
            throw new ConflictException("This employee was changed by someone else. Reload and try again.");
        }
        ExchangeRate rate = requireKnownCurrency(r.currencyCode());
        if (employees.existsByEmployeeCodeAndIdNot(r.employeeCode(), id)) {
            throw new ConflictException("Employee code " + r.employeeCode() + " is already in use.");
        }
        if (employees.existsByEmailAndIdNot(r.email(), id)) {
            throw new ConflictException("Email " + r.email() + " is already in use.");
        }

        employee.update(r.employeeCode(), r.fullName(), r.email(), r.jobTitle(), r.department(),
                r.countryCode(), r.currencyCode(), r.annualSalary(), r.hireDate());
        // Flush now so the response carries the incremented version the client needs for its next edit.
        return EmployeeResponse.from(employees.saveAndFlush(employee), rate);
    }

    @Transactional
    public void delete(long id) {
        if (!employees.existsById(id)) {
            throw new NotFoundException("Employee " + id + " not found.");
        }
        employees.deleteById(id);
    }

    private Employee findOrThrow(long id) {
        return employees.findById(id).orElseThrow(() -> new NotFoundException("Employee " + id + " not found."));
    }

    private ExchangeRate requireKnownCurrency(String currencyCode) {
        return rates.findById(currencyCode)
                .orElseThrow(() -> new BadRequestException("Unsupported currency: " + currencyCode));
    }

    private Map<String, ExchangeRate> ratesByCurrency() {
        return rates.findAll().stream()
                .collect(Collectors.toMap(ExchangeRate::getCurrencyCode, Function.identity()));
    }
}
