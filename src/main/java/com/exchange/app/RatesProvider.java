package com.exchange.app;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class RatesProvider {
    private ForeignExchangeRatesApiClient apiClient;

    public RatesProvider(ForeignExchangeRatesApiClient apiClient) {

        this.apiClient = apiClient;
    }

    public Double getExchangeRateInEUR(Currency requested) {
        try {
            return apiClient.getLatestRates().get(requested.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            throw new CurrencyNotSupportedException("Currency is not supported: " + requested.getCurrencyCode());
        }
    }

    public Double getExchangeRate(Currency requested, Currency exchanged) {
        return apiClient.getLatestRates(exchanged.getCurrencyCode()).get(requested.getCurrencyCode());
    }

    public Double getExchangeRateinEURforSpecificDate(DateTime date, Currency requested) {
        try {
            return apiClient.getHistoricalRates(date).get(requested.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            throw new CurrencyNotSupportedException("Currency is not supported: " + requested.getCurrencyCode()) ;
        }
    }

    public List<Double> getExchangeRatesInEURForSpecificPeriodOfTime(DateTime startDate, DateTime endDate, Currency requested) {
        List<ExchangeRates> listExchangeRates = apiClient.getHistoricalRates(startDate, endDate);

        ArrayList<Double> rates = new ArrayList<>();

        for(ExchangeRates exchangeRates: listExchangeRates) {
            rates.add(exchangeRates.get(requested.getCurrencyCode()));
        }

        return rates;
    }
}
