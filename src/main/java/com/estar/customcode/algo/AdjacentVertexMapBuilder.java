package com.estar.customcode.algo;

import com.estar.customcode.model.Edge;
import com.estar.customcode.model.Vertex;
import com.estar.orderbook.model.CurrencyPair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AdjacentVertexMapBuilder {
    /**
     * Creates adjacent vertices for a given graph also compute the -log value for the vertex weightage
     * a
     *
     * @param currencyMatrix
     * @return
     */
    public Map<Vertex, List<Edge>> build(Map<CurrencyPair, BigDecimal[]> currencyMatrix) {
        Map<Vertex, List<Edge>> adjacentVertexMap = new HashMap<>();
        for (Map.Entry<CurrencyPair, BigDecimal[]> currencyEntry : currencyMatrix.entrySet()) {
            CurrencyPair currencyPair = currencyEntry.getKey();
            String baseCurrency = currencyPair.getBaseCurrency().name();
            String quoteCurrency = currencyPair.getQuoteCurrency().name();
            BigDecimal buy = currencyEntry.getValue()[0].setScale(4, RoundingMode.HALF_UP);
            BigDecimal sell = currencyEntry.getValue()[1].setScale(4, RoundingMode.HALF_UP);
            addEdge(adjacentVertexMap, new Vertex(baseCurrency), new Vertex(quoteCurrency), buy);
            addEdge(adjacentVertexMap, new Vertex(quoteCurrency), new Vertex(baseCurrency), sell);
        }
        return adjacentVertexMap;
    }

    private void addEdge(Map<Vertex, List<Edge>> adjacentVertexMap, Vertex start, Vertex end, BigDecimal weight) {
        Edge edge = new Edge(start, end, modifyEdgeWeight(weight));
        adjacentVertexMap.putIfAbsent(start, new LinkedList<>());
        adjacentVertexMap.get(start).add(edge);
    }

    private BigDecimal modifyEdgeWeight(BigDecimal weight) {
        return new BigDecimal(-Math.log(weight.doubleValue())).setScale(4, RoundingMode.HALF_UP);
    }
}
