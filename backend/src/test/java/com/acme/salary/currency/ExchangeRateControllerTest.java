package com.acme.salary.currency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExchangeRateRepository rates;

    @Test
    void listsSupportedCurrenciesWithTheirSnapshotDate() throws Exception {
        when(rates.findAll(any(Sort.class))).thenReturn(List.of(
                new ExchangeRate("INR", new BigDecimal("0.012"), LocalDate.of(2026, 1, 1)),
                new ExchangeRate("USD", BigDecimal.ONE, LocalDate.of(2026, 1, 1))));

        mvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currencyCode").value("INR"))
                .andExpect(jsonPath("$[0].asOf").value("2026-01-01"))
                .andExpect(jsonPath("$.length()").value(2));
    }
}
