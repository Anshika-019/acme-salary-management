package com.acme.salary.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(nullable = false, length = 100)
    private String department;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    // Kept as a plain code rather than @ManyToOne: we never need the rate when
    // reading an employee, and the database foreign key still enforces validity.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    /** Annual gross base salary in local currency. */
    @Column(name = "annual_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal annualSalary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Employee() {
        // required by JPA
    }

    public Employee(String employeeCode, String fullName, String email, String jobTitle,
                    String department, String countryCode, String currencyCode,
                    BigDecimal annualSalary, LocalDate hireDate) {
        update(employeeCode, fullName, email, jobTitle, department, countryCode, currencyCode,
                annualSalary, hireDate);
    }

    /** The single place where an employee's editable fields change. */
    public void update(String employeeCode, String fullName, String email, String jobTitle,
                       String department, String countryCode, String currencyCode,
                       BigDecimal annualSalary, LocalDate hireDate) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.countryCode = countryCode;
        this.currencyCode = currencyCode;
        this.annualSalary = annualSalary;
        this.hireDate = hireDate;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmployeeCode() { return employeeCode; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getJobTitle() { return jobTitle; }
    public String getDepartment() { return department; }
    public String getCountryCode() { return countryCode; }
    public String getCurrencyCode() { return currencyCode; }
    public BigDecimal getAnnualSalary() { return annualSalary; }
    public LocalDate getHireDate() { return hireDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
