package com.estar.customcode.algo;

import com.estar.customcode.model.Graph;
import com.estar.orderbook.model.CurrencyPair;

import java.math.BigDecimal;
import java.util.Map;

public record AlgoRunner(CurrencyPairGraphBuilder currencyPairGraphBuilder,
                         ArbitragePathFinder arbitragePathFinder) {

    public Map<CurrencyPair, BigDecimal> runAlgorithm(Map<CurrencyPair, BigDecimal[]> instrumentData) {
        Graph graph = currencyPairGraphBuilder.build(instrumentData);
        return arbitragePathFinder.findCycles(graph);
    }
}
