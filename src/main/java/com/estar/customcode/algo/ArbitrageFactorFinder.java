package com.estar.customcode.algo;

import com.estar.customcode.model.Edge;
import com.estar.customcode.model.Graph;
import com.estar.customcode.model.Vertex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Traverse the negative cycle and add on the weight convert back from -log and subtract with
 * 1 to get the value
 *
 */
public class ArbitrageFactorFinder {
    public BigDecimal findFactor(Graph graph, List<Vertex> cycles) {
        BigDecimal totalArbitrage = new BigDecimal(0);
        for (int j = 1; j < cycles.size(); j++) {
            Vertex u = cycles.get(j - 1);
            Vertex v = cycles.get(j);
            List<Edge> adjVertex = graph.getVertexAdjacencyMap().get(u);
            for (Edge e : adjVertex) {
                if (e.getStartVertex().equals(u) && e.getEndVertex().equals(v)) {
                    totalArbitrage = totalArbitrage.add(e.getWeight());
                    break;
                }
            }
        }
        return transformArbitrageValue(totalArbitrage);
    }

    /**
     * Converting back form -log to real value and subtracting from 1 just to have the difference
     *
     * @param totalArbitrage
     * @return
     */
    private BigDecimal transformArbitrageValue(BigDecimal totalArbitrage) {
        double unLoggedArbitrage = Math.pow(2.0, (totalArbitrage.doubleValue() * -1)) - 1;
        BigDecimal value = new BigDecimal(unLoggedArbitrage).setScale(3, RoundingMode.CEILING);
        return value;
    }
}
