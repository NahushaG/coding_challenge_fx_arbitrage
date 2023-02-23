package com.estar.customcode.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class Graph {
    private final Map<Vertex, List<Edge>>vertexAdjacencyMap;
    private final List<Vertex> vertices;
    private final List<Edge> edges;
}
