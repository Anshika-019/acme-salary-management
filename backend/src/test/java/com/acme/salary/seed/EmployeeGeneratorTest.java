package com.acme.salary.seed;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeGeneratorTest {

    // Same snapshot as V2__seed_exchange_rates.sql
    private static final Map<String, BigDecimal> RATES = Map.of(
            "USD", new BigDecimal("1.00"),
            "GBP", new BigDecimal("1.27"),
            "EUR", new BigDecimal("1.10"),
            "CAD", new BigDecimal("0.73"),
            "AUD", new BigDecimal("0.66"),
            "SGD", new BigDecimal("0.74"),
            "JPY", new BigDecimal("0.0067"),
            "INR", new BigDecimal("0.012"));

    private final EmployeeGenerator generator = new EmployeeGenerator(RATES);

    @Test
    void generatesRequestedNumberOfEmployees() {
        assertThat(generator.generate(500, 42)).hasSize(500);
    }

    @Test
    void sameSeedProducesIdenticalData() {
        assertThat(generator.generate(200, 42)).isEqualTo(generator.generate(200, 42));
    }

    @Test
    void differentSeedsProduceDifferentData() {
        assertThat(generator.generate(200, 1)).isNotEqualTo(generator.generate(200, 2));
    }

    @Test
    void employeeCodesAndEmailsAreUniqueAcrossFullDataset() {
        List<GeneratedEmployee> employees = generator.generate(10_000, 42);

        assertThat(new HashSet<>(employees.stream().map(GeneratedEmployee::employeeCode).toList())).hasSize(10_000);
        assertThat(new HashSet<>(employees.stream().map(GeneratedEmployee::email).toList())).hasSize(10_000);
    }

    @Test
    void emailsAreAsciiEvenForAccentedNames() {
        assertThat(generator.generate(10_000, 42))
                .allSatisfy(e -> assertThat(e.email()).matches("[a-z]+\\.[a-z]+\\.\\d{5}@acme\\.com"));
    }

    @Test
    void currencyAlwaysMatchesCountry() {
        Map<String, String> currencyByCountry = SeedCatalog.COUNTRIES.stream()
                .collect(Collectors.toMap(SeedCatalog.Country::code, SeedCatalog.Country::currency));

        assertThat(generator.generate(2_000, 42))
                .allSatisfy(e -> assertThat(e.currencyCode()).isEqualTo(currencyByCountry.get(e.countryCode())));
    }

    @Test
    void salariesArePositiveAndRoundedToHundreds() {
        assertThat(generator.generate(2_000, 42)).allSatisfy(e -> {
            assertThat(e.annualSalary()).isPositive();
            assertThat(e.annualSalary().remainder(BigDecimal.valueOf(100))).isZero();
        });
    }

    @Test
    void typicalSalaryTracksRoleBaseAndCountryFactor() {
        // US Software Engineer base is 120,000 USD with cost factor 1.0
        List<BigDecimal> salaries = generator.generate(10_000, 42).stream()
                .filter(e -> e.countryCode().equals("US") && e.jobTitle().equals("Software Engineer"))
                .map(GeneratedEmployee::annualSalary)
                .sorted()
                .toList();

        BigDecimal median = salaries.get(salaries.size() / 2);
        assertThat(median).isBetween(new BigDecimal("114000"), new BigDecimal("126000"));
    }

    @Test
    void hireDatesFallWithinCompanyHistory() {
        assertThat(generator.generate(2_000, 42)).allSatisfy(e ->
                assertThat(e.hireDate()).isBetween(LocalDate.of(2012, 1, 1), LocalDate.of(2026, 6, 30)));
    }

    @Test
    void rejectsMissingExchangeRate() {
        assertThatThrownBy(() -> new EmployeeGenerator(Map.of("USD", BigDecimal.ONE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing exchange rate");
    }
}
