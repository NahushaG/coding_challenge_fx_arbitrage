package com.estar.customcode.algo;

import com.estar.customcode.model.Edge;
import com.estar.customcode.model.Graph;
import com.estar.customcode.model.Vertex;

import java.math.BigDecimal;
import java.util.Map;

public class EdgeRelaxer {
    /**
     * Calculate the traversed node(value infinity)  from the starting node(0)
     * and add the weightage to the traversed node. This process is carried
     * till (N-1) cycle and in each cycle all the nodes are traversed
     *
     * @param graph
     * @param distances
     * @param predecessor
     */
    public void relaxEdges(Graph graph, Map<Vertex, Double> distances, Map<Vertex, Vertex> predecessor) {
        for (int i = 0; i < distances.size() - 1; i++) {
            for (Edge e : graph.getEdges()) {
                if (distances.get(e.getEndVertex()) > distances.get(e.getStartVertex()) + e.getWeight().doubleValue()) {
                    distances.put(e.getEndVertex(), distances.get(e.getStartVertex()) + e.getWeight().doubleValue());
                    predecessor.put(e.getEndVertex(), e.getStartVertex());
                }
            }
        }
    }
}
