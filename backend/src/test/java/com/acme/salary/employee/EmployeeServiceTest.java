package com.acme.salary.employee;

import com.acme.salary.common.BadRequestException;
import com.acme.salary.common.ConflictException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.currency.ExchangeRate;
import com.acme.salary.currency.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Business rules of EmployeeService, with repositories mocked out: no Spring, no database. */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final ExchangeRate INR = new ExchangeRate("INR", new BigDecimal("0.012"), LocalDate.of(2026, 1, 1));

    @Mock
    private EmployeeRepository employees;

    @Mock
    private ExchangeRateRepository rates;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(employees, rates);
        lenient().when(rates.findById("INR")).thenReturn(Optional.of(INR));
        lenient().when(employees.saveAndFlush(any(Employee.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createNormalisesInputAndReturnsUsdSalary() {
        EmployeeResponse created = service.create(request("  emp-00042 ", "  Asha@ACME.com ", " in ", "inr", null));

        assertThat(created.employeeCode()).isEqualTo("EMP-00042");
        assertThat(created.email()).isEqualTo("asha@acme.com");
        assertThat(created.countryCode()).isEqualTo("IN");
        assertThat(created.currencyCode()).isEqualTo("INR");
        assertThat(created.annualSalaryUsd()).isEqualByComparingTo("18000.00");
    }

    @Test
    void createRejectsUnsupportedCurrency() {
        when(rates.findById("ZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("EMP-00042", "asha@acme.com", "IN", "ZZZ", null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported currency");
        verify(employees, never()).saveAndFlush(any());
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(employees.existsByEmail("asha@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP-00042", "asha@acme.com", "IN", "INR", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("asha@acme.com");
    }

    @Test
    void createRejectsDuplicateEmployeeCode() {
        when(employees.existsByEmployeeCode("EMP-00042")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("EMP-00042", "asha@acme.com", "IN", "INR", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("EMP-00042");
    }

    @Test
    void updateRequiresVersion() {
        assertThatThrownBy(() -> service.update(1L, request("EMP-00042", "asha@acme.com", "IN", "INR", null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("version");
    }

    @Test
    void updateRejectsStaleVersion() {
        Employee stored = existingEmployee(3L);
        when(employees.findById(1L)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.update(1L, request("EMP-00042", "asha@acme.com", "IN", "INR", 2L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("changed by someone else");
    }

    @Test
    void updateAppliesChangesWhenVersionMatches() {
        Employee stored = existingEmployee(3L);
        when(employees.findById(1L)).thenReturn(Optional.of(stored));

        EmployeeRequest raise = new EmployeeRequest("EMP-00042", "Asha Verma", "asha@acme.com",
                "Senior Software Engineer", "Engineering", "IN", "INR",
                new BigDecimal("2000000"), LocalDate.of(2022, 4, 1), 3L);
        EmployeeResponse updated = service.update(1L, raise);

        assertThat(updated.jobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(updated.annualSalary()).isEqualByComparingTo("2000000");
    }

    @Test
    void getThrowsNotFoundForUnknownId() {
        when(employees.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteThrowsNotFoundForUnknownId() {
        when(employees.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
        verify(employees, never()).deleteById(any());
    }

    private static EmployeeRequest request(String code, String email, String country, String currency, Long version) {
        return new EmployeeRequest(code, "Asha Verma", email, "Software Engineer", "Engineering",
                country, currency, new BigDecimal("1500000"), LocalDate.of(2022, 4, 1), version);
    }

    private static Employee existingEmployee(long version) {
        Employee employee = new Employee("EMP-00042", "Asha Verma", "asha@acme.com", "Software Engineer",
                "Engineering", "IN", "INR", new BigDecimal("1500000"), LocalDate.of(2022, 4, 1));
        ReflectionTestUtils.setField(employee, "id", 1L);
        ReflectionTestUtils.setField(employee, "version", version);
        return employee;
    }
}
