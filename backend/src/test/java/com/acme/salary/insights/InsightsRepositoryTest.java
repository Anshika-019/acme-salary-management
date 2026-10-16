package com.acme.salary.insights;

import com.acme.salary.TestcontainersConfiguration;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Analytics SQL against real Postgres with a tiny, hand-checkable dataset:
 * five Indian engineers (one paid far above the rest), plus two US employees.
 * INR rate is 0.012 (from the V2 migration), so 1,000,000 INR = 12,000 USD.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class InsightsRepositoryTest {

    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.3");

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private InsightsRepository insights;

    @BeforeEach
    void seed() {
        employees.saveAllAndFlush(List.of(
                employee(1, "Software Engineer", "Engineering", "IN", "INR", "1000000"),
                employee(2, "Software Engineer", "Engineering", "IN", "INR", "1200000"),
                employee(3, "Software Engineer", "Engineering", "IN", "INR", "1400000"),
                employee(4, "Software Engineer", "Engineering", "IN", "INR", "1600000"),
                employee(5, "Software Engineer", "Engineering", "IN", "INR", "5000000"),
                employee(6, "Software Engineer", "Engineering", "US", "USD", "120000"),
                employee(7, "Account Executive", "Sales", "US", "USD", "90000")));
    }

    @Test
    void summaryAggregatesWholeOrganisationInUsd() {
        SalarySummary summary = insights.summary(InsightsFilter.none());

        assertThat(summary.headcount()).isEqualTo(7);
        assertThat(summary.totalPayrollUsd()).isEqualByComparingTo("332400.00");
        assertThat(summary.medianUsd()).isEqualByComparingTo("19200.00");
        assertThat(summary.countries()).isEqualTo(2);
        assertThat(summary.departments()).isEqualTo(2);
    }

    @Test
    void medianResistsOutlierWhileAverageDoesNot() {
        GroupStats india = insights.statsBy(GroupBy.COUNTRY, new InsightsFilter("IN", null, null)).getFirst();

        assertThat(india.headcount()).isEqualTo(5);
        assertThat(india.medianUsd()).isEqualByComparingTo("16800.00");
        assertThat(india.averageUsd()).isEqualByComparingTo("24480.00");
    }

    @Test
    void singleCurrencyGroupReportsLocalMedian() {
        GroupStats india = insights.statsBy(GroupBy.COUNTRY, new InsightsFilter("IN", null, null)).getFirst();

        assertThat(india.localCurrency()).isEqualTo("INR");
        assertThat(india.medianLocal()).isEqualByComparingTo("1400000.00");
    }

    @Test
    void mixedCurrencyGroupHasNoLocalMedian() {
        GroupStats engineering = insights.statsBy(GroupBy.DEPARTMENT, InsightsFilter.none()).stream()
                .filter(g -> g.group().equals("Engineering"))
                .findFirst()
                .orElseThrow();

        assertThat(engineering.localCurrency()).isNull();
        assertThat(engineering.medianLocal()).isNull();
    }

    @Test
    void groupsAreOrderedByMedianDescending() {
        List<String> groups = insights.statsBy(GroupBy.DEPARTMENT, InsightsFilter.none()).stream()
                .map(GroupStats::group)
                .toList();

        assertThat(groups).containsExactly("Sales", "Engineering");
    }

    @Test
    void flagsEmployeePaidFarAbovePeerMedian() {
        OutlierReport report = insights.outliers(InsightsFilter.none(), DEFAULT_THRESHOLD, 5, 50);

        assertThat(report.totalCount()).isEqualTo(1);
        Outlier outlier = report.outliers().getFirst();
        assertThat(outlier.employeeCode()).isEqualTo("EMP-90005");
        assertThat(outlier.peerMedian()).isEqualByComparingTo("1400000.00");
        assertThat(outlier.peerCount()).isEqualTo(5);
        assertThat(outlier.deviationPercent()).isEqualByComparingTo("257.1");
    }

    @Test
    void smallPeerGroupsNeverProduceOutliers() {
        // Each US role has a single employee: no meaningful peer median exists.
        OutlierReport report = insights.outliers(new InsightsFilter("US", null, null), DEFAULT_THRESHOLD, 5, 50);

        assertThat(report.totalCount()).isZero();
    }

    @Test
    void higherThresholdReportsFewerOutliers() {
        OutlierReport report = insights.outliers(InsightsFilter.none(), new BigDecimal("3.0"), 5, 50);

        assertThat(report.outliers()).isEmpty();
    }

    private static Employee employee(int n, String title, String department,
                                     String country, String currency, String salary) {
        return new Employee("EMP-9000" + n, "Person " + n, "person" + n + "@acme.com", title, department,
                country, currency, new BigDecimal(salary), LocalDate.of(2022, 1, 1));
    }
}
