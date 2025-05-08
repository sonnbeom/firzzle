package com.firzzle.llm.domain;

import java.util.*;

import lombok.Data;

@Data
public class QdrantSearchResponse {
    private List<Map<String, Object>> result;
    private String status;
    private double time;
}