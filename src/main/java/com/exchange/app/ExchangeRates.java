package com.exchange.app;

import java.time.LocalDate;
import java.util.Map;

public class ExchangeRates {
    private String base;
    private LocalDate date;
    private Map<String, Double> rates;

    public ExchangeRates(String base, LocalDate date, Map<String, Double> rates) {
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    public Double get(String currency) {
        return rates.get(currency);
    }

    public LocalDate getDate() {
        return date;
    }
}

