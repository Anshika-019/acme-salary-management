package com.acme.salary.currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateTest {

    private static final LocalDate AS_OF = LocalDate.of(2026, 1, 1);

    @Test
    void convertsLocalAmountToUsd() {
        ExchangeRate inr = new ExchangeRate("INR", new BigDecimal("0.012"), AS_OF);

        assertThat(inr.toUsd(new BigDecimal("1000000"))).isEqualByComparingTo("12000.00");
    }

    @Test
    void roundsConvertedAmountToCents() {
        ExchangeRate jpy = new ExchangeRate("JPY", new BigDecimal("0.00666667"), AS_OF);

        assertThat(jpy.toUsd(new BigDecimal("100"))).isEqualByComparingTo("0.67");
    }

    @Test
    void rejectsNonPositiveRate() {
        assertThatThrownBy(() -> new ExchangeRate("XXX", BigDecimal.ZERO, AS_OF))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
