package com.estar.arbitrage;

import com.estar.customcode.processors.OrderBook;
import com.estar.orderbook.model.CurrencyPair;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public record ResultFormatter(@NonNull Map<CurrencyPair, OrderBook> orderBooks, @NonNull ArbitrageProcessor arbitrageProcessor) {
    public void processArbitrageRecord(Map<CurrencyPair, BigDecimal> arbitrageCurrencyMap) {
        ArbitrageRecord arbitrageRecord = null;
        Map<BigDecimal, ArbitrageRecord> factorArbitrageMap = new HashMap<>();
        for (Map.Entry<CurrencyPair, BigDecimal> arbitrageCurrency : arbitrageCurrencyMap.entrySet()) {
            CurrencyPair currencyPair = arbitrageCurrency.getKey();
            OrderBook orderBook = orderBooks.get(currencyPair);
            ArbitrageRecord.Element element = new ArbitrageRecord.Element(currencyPair, orderBook.getBestBuy(), orderBook.getBestSell());
            if (factorArbitrageMap.containsKey(arbitrageCurrency.getValue())) {
                arbitrageRecord = factorArbitrageMap.get(arbitrageCurrency.getValue());
                arbitrageRecord.arbitrageElements().add(element);
            } else {
                List<ArbitrageRecord.Element> elements = new ArrayList<>();
                elements.add(element);
                arbitrageRecord = new ArbitrageRecord(arbitrageCurrency.getValue(), elements);
                factorArbitrageMap.put(arbitrageCurrency.getValue(), arbitrageRecord);
            }
        }
        arbitrageProcessor.reportArbitrage(arbitrageRecord);
    }
}
