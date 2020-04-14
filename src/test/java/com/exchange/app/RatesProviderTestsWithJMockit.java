package com.exchange.app;

import mockit.Expectations;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class RatesProviderTestsWithJMockit extends MockUp<ForeignExchangeRatesApiClient> {

    private static final String SEK = "SEK";
    private static final String USD = "USD";
    private static final String EUR = "EUR";


    @Test
    void shouldReturnEURRateInUSD(@Mocked ForeignExchangeRatesApiClient apiClient) {
        //given
        Map<String, Double> rates = new HashMap<String, Double>() {};
        rates.put(EUR, 0.8);
        rates.put(SEK, 15.30);

        RatesProvider ratesProvider = new RatesProvider(apiClient);

        new Expectations(){{
            apiClient.getLatestRates(anyString);
            result = new ExchangeRates(USD, LocalDate.now(), rates);
            times = 1;
        }};

        //when
        Double rateEUR = ratesProvider.getExchangeRate(Currency.getInstance(EUR), Currency.getInstance(USD));

        //then
        new Verifications(){{
            Assertions.assertThat(rateEUR).isEqualTo(0.8);
        }};
    }
}
