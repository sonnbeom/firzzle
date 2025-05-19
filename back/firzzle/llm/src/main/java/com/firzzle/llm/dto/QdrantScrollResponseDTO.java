package com.firzzle.llm.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class QdrantScrollResponseDTO {
    // 실제 Qdrant가 result 아래에 wrapping 객체로 내려주는 필드 타입을 맞추세요.
    // 예: { "result": { "points": [ {...}, {...} ], "next_page_offset": ... }, ... }
    private Map<String, Object> result;
    // status, time 등 나머지 필드는 필요하다면 추가

}
