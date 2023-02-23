package com.estar.customcode.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Edge {
    private final Vertex startVertex;
    private final Vertex endVertex;
    private final BigDecimal weight;
}
