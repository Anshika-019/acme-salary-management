package com.acme.salary.currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A static snapshot of how many USD one unit of a currency is worth.
 * Used only for analytics; salaries are always stored in local currency.
 */
@Entity
@Table(name = "exchange_rate")
public class ExchangeRate {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "rate_to_usd", nullable = false, precision = 18, scale = 8)
    private BigDecimal rateToUsd;

    @Column(name = "as_of", nullable = false)
    private LocalDate asOf;

    protected ExchangeRate() {
        // required by JPA
    }

    public ExchangeRate(String currencyCode, BigDecimal rateToUsd, LocalDate asOf) {
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode");
        this.rateToUsd = Objects.requireNonNull(rateToUsd, "rateToUsd");
        this.asOf = Objects.requireNonNull(asOf, "asOf");
        if (rateToUsd.signum() <= 0) {
            throw new IllegalArgumentException("rateToUsd must be positive");
        }
    }

    /** Converts an amount in this currency to USD, rounded to cents. */
    public BigDecimal toUsd(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return amount.multiply(rateToUsd).setScale(2, RoundingMode.HALF_UP);
    }

    public String getCurrencyCode() { return currencyCode; }
    public BigDecimal getRateToUsd() { return rateToUsd; }
    public LocalDate getAsOf() { return asOf; }
}
