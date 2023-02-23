package com.estar.customcode.algo;

import com.estar.customcode.model.Edge;
import com.estar.customcode.model.Graph;
import com.estar.customcode.model.Vertex;
import com.estar.orderbook.model.CurrencyPair;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ArbitragePathFinder {

    private final EdgeRelaxer edgeRelaxer;
    private final Map<Vertex, Double> distances = new HashMap<>();
    private final Map<Vertex, Vertex> predecessor = new HashMap<>();
    @NonNull
    private final ArbitrageFactorFinder arbitrageFactorFinder;
    @NonNull
    private final Map<String, CurrencyPair> currencyCodeCurrencyPair;


    /**
     * calibrates the graph to see if there are negative cycle (which detect the presence
     * of arbitrage case) and returns a map of the currency pair which has those case along wih
     * arbitrage factor
     *
     * @param graph
     * @return
     */
    public Map<CurrencyPair, BigDecimal> findCycles(Graph graph) {
        Map<CurrencyPair, BigDecimal> cycle = new HashMap<>();
        for (Vertex v : graph.getVertices()) {
            Map<CurrencyPair, BigDecimal> cycleAtVertex = findArbitrageCurrency(graph, v);
            for (Map.Entry<CurrencyPair, BigDecimal> cycleAtVertexEntry : cycleAtVertex.entrySet()) {
                if (cycle.containsKey(cycleAtVertexEntry.getKey())) {
                    BigDecimal existingFactor = cycle.get(cycleAtVertexEntry.getKey());
                    if (cycleAtVertexEntry.getValue().compareTo(existingFactor) > 0) {
                        cycle.put(cycleAtVertexEntry.getKey(), cycleAtVertexEntry.getValue());
                    }
                } else {
                    cycle.put(cycleAtVertexEntry.getKey(), cycleAtVertexEntry.getValue());
                }
            }
        }
        return cycle;
    }

    /**
     * Based on the algo marks all the node to infinity except for the starting node.
     * then perform edge relaxation for (node-1) time and post that if we have negative
     * cycle than computes the arbitrage factor
     *
     * @param graph
     * @param sourceVertex
     * @return
     */
    private Map<CurrencyPair, BigDecimal> findArbitrageCurrency(Graph graph, Vertex sourceVertex) {
        for (Vertex v : graph.getVertices()) {
            distances.put(v, Double.POSITIVE_INFINITY);
            predecessor.put(v, v);
        }
        distances.put(sourceVertex, 0.0);
        edgeRelaxer.relaxEdges(graph, distances, predecessor);
        return getArbitrageCurrencyWithFactor(graph);
    }

    /**
     * Arbitrage factor is calculated in following order for a negative cycle (USD -> EUR)
     * Start traversing from source node EUR keep adding it to list ,
     * check predecessor if present and if so add it to the list till either the destination
     * is met or visited node is in the list.
     *
     * Give the list to factor finder to get the arbitrage
     *
     * @param graph
     * @return
     */
    private Map<CurrencyPair, BigDecimal> getArbitrageCurrencyWithFactor(Graph graph) {
        HashMap<CurrencyPair, BigDecimal> currencyArbitrageFactorMap = new HashMap<>();
        Set<Vertex> seenVertices = new HashSet<>();
        for (Edge e : graph.getEdges()) {
            if (seenVertices.contains(e.getEndVertex())) continue;

            if (distances.get(e.getEndVertex()).doubleValue() > distances.get(e.getStartVertex()).doubleValue() + e.getWeight().doubleValue()) {
                String arbitrageCurrencyPair = e.getStartVertex().getId() + "_" + e.getEndVertex().getId();
                CurrencyPair currencyPair = currencyCodeCurrencyPair.get(arbitrageCurrencyPair);
                ArrayList<Vertex> newCycle = new ArrayList<>();
                Vertex vertex = e.getStartVertex();
                do {
                    seenVertices.add(vertex);
                    newCycle.add(vertex);
                    vertex = predecessor.get(vertex);
                } while (vertex != e.getStartVertex() && !seenVertices.contains(vertex));
                BigDecimal arbitrageFactor = arbitrageFactorFinder.findFactor(graph, newCycle);
                if (arbitrageFactor.compareTo(new BigDecimal(0)) > 0) {
                    currencyArbitrageFactorMap.put(currencyPair, arbitrageFactor);
                }
            }
        }
        return currencyArbitrageFactorMap;
    }

}
