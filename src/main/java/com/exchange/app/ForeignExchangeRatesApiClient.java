package com.exchange.app;

import java.time.LocalDate;
import java.util.List;

public interface ForeignExchangeRatesApiClient {

    ExchangeRates getLatestRates();

    List<ExchangeRates> getLatestRatesForCurrencies(List<String> symbols);

    ExchangeRates getLatestRates(String base);

    ExchangeRates getHistoricalRates(LocalDate date);

    List<ExchangeRates> getHistoricalRates(LocalDate start_at, LocalDate end_at);

    List<ExchangeRates> getHistoricalRates(LocalDate start_at, LocalDate end_at, List<String> symbols);

    List<ExchangeRates> getHistoricalRates(LocalDate start_at, LocalDate end_at, String base);
}
