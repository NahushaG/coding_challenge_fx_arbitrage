package com.estar.customcode.algo;

import com.estar.customcode.model.Edge;
import com.estar.customcode.model.Graph;
import com.estar.customcode.model.Vertex;
import com.estar.orderbook.model.CurrencyPair;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds the graph needed to be used by the algorithm. vertex is the currency and
 * edges are the currency pair, weight is -(log(rate))
 */
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CurrencyPairGraphBuilder {
    @NonNull
    private final AdjacentVertexMapBuilder adjacentVertMapBuilder;

    public Graph build(Map<CurrencyPair, BigDecimal[]> instrumentData) {
        Map<Vertex, List<Edge>> vertexAdjacencyMap = adjacentVertMapBuilder.build(instrumentData);

        return Graph.builder()
                .vertexAdjacencyMap(vertexAdjacencyMap)
                .vertices(createVertexList(vertexAdjacencyMap))
                .edges(createEdgeList(vertexAdjacencyMap))
                .build();
    }

    private List<Vertex> createVertexList(Map<Vertex, List<Edge>> vertexAdjacencyMap) {
        return new ArrayList<>(vertexAdjacencyMap.keySet());
    }

    private List<Edge> createEdgeList(Map<Vertex, List<Edge>> vertexAdjacencyMap) {
        List<Edge> allEdges = new ArrayList<>();
        for (Map.Entry<Vertex, List<Edge>> list : vertexAdjacencyMap.entrySet()) {
            allEdges.addAll(list.getValue());
        }
        return allEdges;
    }
}
