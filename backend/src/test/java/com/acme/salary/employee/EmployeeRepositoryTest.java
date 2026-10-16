package com.acme.salary.employee;

import com.acme.salary.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs against a real Postgres (Testcontainers) so the Flyway schema,
 * entity mappings and database constraints are verified together.
 * Exchange rates come from the V2 migration, exactly as in production.
 * Each test runs in a transaction that is rolled back, keeping tests independent.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employees;

    @Test
    void savesAndLoadsEmployee() {
        Employee saved = employees.saveAndFlush(employee("EMP-90001", "asha@acme.com", "INR"));

        Employee loaded = employees.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getFullName()).isEqualTo("Asha Verma");
        assertThat(loaded.getAnnualSalary()).isEqualByComparingTo("1500000.00");
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getVersion()).isZero();
    }

    @Test
    void rejectsDuplicateEmail() {
        employees.saveAndFlush(employee("EMP-90001", "asha@acme.com", "INR"));

        assertThatThrownBy(() -> employees.saveAndFlush(employee("EMP-90002", "asha@acme.com", "INR")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsCurrencyWithoutExchangeRate() {
        assertThatThrownBy(() -> employees.saveAndFlush(employee("EMP-90001", "asha@acme.com", "ZZZ")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Employee employee(String code, String email, String currency) {
        return new Employee(code, "Asha Verma", email, "Software Engineer", "Engineering",
                "IN", currency, new BigDecimal("1500000.00"), LocalDate.of(2022, 4, 1));
    }
}
