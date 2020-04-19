package com.exchange.app;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class RatesProviderTests {

    private static final String SEK = "SEK";
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    private ForeignExchangeRatesApiClient apiClient;
    private RatesProvider provider;
    private Random random = new Random(System.nanoTime());

    @BeforeEach
    void setUp() {
        apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
        provider = new RatesProvider(apiClient);
    }

    @Test
    @DisplayName("For default currency (EUR) returns USD rate")
    void test1() {

        //given
        ExchangeRates exchangeRates = initializeExchangeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(exchangeRates);

        //when
        Double rateUSD = provider.getExchangeRateInEUR(Currency.getInstance(USD));

        //then
        assertThat(exchangeRates.get(USD)).isEqualTo(rateUSD);
    }

    @Test
    @DisplayName("For default currency (EUR) returns all rates")
    void test2() {
        //given
        ExchangeRates exchangeRates = initializeExchangeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(exchangeRates);

        //when
        Double rateSEK = provider.getExchangeRateInEUR(Currency.getInstance(SEK));
        Double rateUSD = provider.getExchangeRateInEUR(Currency.getInstance(USD));

        //then
        assertAll(
                () -> assertEquals(exchangeRates.get(USD), rateUSD, "USD rate should be included"),
                () -> assertEquals(exchangeRates.get(SEK), rateSEK, "SEK rate should be included")
        );
    }

    @Test
    void shouldReturnCurrencyExchangeRatesForOtherCurrency() {
        //given
        ExchangeRates exchangeRates = initializeExchangeRates();
        List<String> currencies = Arrays.asList(EUR, SEK, USD);

        Mockito.when(apiClient.getLatestRates(anyString())).thenAnswer(
                new Answer<ExchangeRates>() {

                    @Override
                    public ExchangeRates answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Object base = invocationOnMock.getArgument(0);
                        if (currencies.contains(base)) {
                            return exchangeRates;
                        } else {
                            throw new CurrencyNotSupportedException("Not supported: " + base);
                        }
                    }
                }
        );

        //when
        Double rate = provider.getExchangeRate(Currency.getInstance(SEK), Currency.getInstance(USD));

        //then
        assertThat(exchangeRates.get(SEK)).isEqualTo(rate);
    }

    @Test
    void shouldThrowExceptionWhenCurrencyNotSupported() {
        //given
        Mockito.when(apiClient.getLatestRates()).thenThrow(new IllegalArgumentException());

        //then
        CurrencyNotSupportedException actual =
                assertThrows(CurrencyNotSupportedException.class,
                        () -> provider.getExchangeRateInEUR(Currency.getInstance("CHF")));

        assertEquals("Currency is not supported: CHF", actual.getMessage());
    }

    @Test
    void shouldGetRatesOnlyOnce() {
        //given
        ExchangeRates exchangeRates = initializeExchangeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(exchangeRates);

        //when
        provider.getExchangeRateInEUR(Currency.getInstance(SEK));

        //then
        Mockito.verify(apiClient).getLatestRates();
    }

    @Test
    @DisplayName("For default currency (EUR) returns historical USD rate for specific date")
    void should_return_historical_USD_rate_for_default_currency_for_specific_date() {
        //given
        ExchangeRates exchangeRates = initializeExchangeRates();

        DateTime date = new DateTime(2020,1,3,0,0);
        Mockito.when(apiClient.getHistoricalRates(date)).thenReturn(exchangeRates);


        //when
        Double rateUSD = provider.getExchangeRateinEURforSpecificDate(date, Currency.getInstance(USD));

        //then
        assertThat(exchangeRates.get(USD)).isEqualTo(rateUSD);
    }

    @Test
    @DisplayName("For default currency (EUR) returns historical USD rates for specific period of time ")
    void test() {
        //given
        ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);

        List<ExchangeRates> listExchangeRates = initializeExchangeRatesForFirst3DaysOfJanuary();
        ExchangeRates januaryFirst2020exchangeRates = listExchangeRates.get(0);
        ExchangeRates januarySecond20202xchangeRates = listExchangeRates.get(1);
        ExchangeRates januaryThird2020exchangeRates = listExchangeRates.get(2);

        Map<String, DateTime> dates = initializeDates();

        Mockito.when(apiClient.getHistoricalRates(dates.get("januaryFirst2020"), dates.get("januaryThird2020"))).thenReturn(listExchangeRates);

        RatesProvider provider = new RatesProvider(apiClient);

        //when
        List<Double> ratesUSD = provider.getExchangeRatesInEURForSpecificPeriodOfTime(dates.get("januaryFirst2020"), dates.get("januaryThird2020"), Currency.getInstance(USD));
        Double januaryFirst2020ratedUSD = ratesUSD.get(0);
        Double januarySecond2020ratesUSD = ratesUSD.get(1);
        Double januaryThird2020ratesUSD = ratesUSD.get(2);

        //then
        assertAll(
                () -> assertThat(januaryFirst2020exchangeRates.get(USD)).isEqualTo(januaryFirst2020ratedUSD),
                () -> assertThat(januarySecond20202xchangeRates.get(USD)).isEqualTo(januarySecond2020ratesUSD),
                () -> assertThat(januaryThird2020exchangeRates.get(USD)).isEqualTo(januaryThird2020ratesUSD)
        );

    }

    private Map<String, DateTime> initializeDates() {

        DateTime januaryFirst2020 = new DateTime(2020, 1, 1, 0, 0);
        DateTime januarySecond2020 = new DateTime(2020, 1, 2, 0, 0);
        DateTime januaryThird2020 = new DateTime(2020, 1, 3, 0, 0);

        return new HashMap<String, DateTime>() {{
            put("januaryFirst2020", januaryFirst2020);
            put("januarySecond2020", januarySecond2020);
            put("januaryThird2020", januaryThird2020);
        }};

    }

    private List<ExchangeRates> initializeExchangeRatesForFirst3DaysOfJanuary() {

        Map<String, DateTime> dates = initializeDates();

        DateTime januaryFirst2020 = dates.get("januaryFirst2020");
        DateTime januarySecond2020 = dates.get("januarySecond2020");
        DateTime januaryThird2020 = dates.get("januaryThird2020");

        ExchangeRates firstExchange = new RatesForCurrencyForDayBuilder().
                basedEUR().
                forDay(januaryFirst2020).
                addRate(USD, 1.20).
                addRate(SEK, 10.30).
                build();
        ExchangeRates secondExchange = new RatesForCurrencyForDayBuilder().
                basedEUR().
                forDay(januarySecond2020).
                addRate(USD, 1.21).
                addRate(SEK, 10.31).
                build();
        ExchangeRates thirdExchange = new RatesForCurrencyForDayBuilder().
                basedEUR().
                forDay(januaryThird2020).
                addRate(USD, 1.22).
                addRate(SEK, 10.32).
                build();

        return new ArrayList<ExchangeRates>() {{
            add(firstExchange);
            add(secondExchange);
            add(thirdExchange);
        }};
    }

    private ExchangeRates initializeExchangeRates() {
        Map<String, Double> rates = new HashMap<String, Double>() {
        };
        rates.put(USD, random.nextDouble());
        rates.put(SEK, random.nextDouble());
        return initializeExchangeRates(EUR, DateTime.now(), rates);
    }

    private ExchangeRates initializeExchangeRates(String base) {
        Map<String, Double> rates = new HashMap<String, Double>() {
        };
        rates.put(EUR, random.nextDouble());
        rates.put(SEK, random.nextDouble());
        return initializeExchangeRates(base, DateTime.now(), rates);
    }

    private ExchangeRates initializeExchangeRates(String base, DateTime date, Map<String, Double> rates) {
        return new ExchangeRates(base, date, rates);
    }

}