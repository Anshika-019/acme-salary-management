package com.acme.salary.currency;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Read-only list of supported currencies and their USD rates. The UI uses it to offer only
 * currencies the system can convert, and to show which date the USD figures are based on.
 */
@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    record ExchangeRateResponse(String currencyCode, BigDecimal rateToUsd, LocalDate asOf) {
    }

    private final ExchangeRateRepository rates;

    public ExchangeRateController(ExchangeRateRepository rates) {
        this.rates = rates;
    }

    @GetMapping
    public List<ExchangeRateResponse> list() {
        return rates.findAll(Sort.by("currencyCode")).stream()
                .map(r -> new ExchangeRateResponse(r.getCurrencyCode().trim(), r.getRateToUsd(), r.getAsOf()))
                .toList();
    }
}
