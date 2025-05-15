package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Class Name : QdrantSearchResponseDTO.java
 * @Description : Qdrant 벡터 검색 응답 DTO (검색 결과 + 메타 정보 포함)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Schema(description = "Qdrant 벡터 검색 결과 응답 DTO")
public class QdrantSearchResponseDTO {

    @Schema(
        description = "검색 결과 리스트. 각 Map은 벡터와 payload 등의 정보를 포함함",
        example = "[{\"id\": 123, \"score\": 0.95, \"payload\": {\"summary\": \"DDD 설명\"}}]"
    )
    private List<Map<String, Object>> result;

    @Schema(description = "응답 상태", example = "ok")
    private String status;

    @Schema(description = "검색 소요 시간 (초 단위)", example = "0.0023")
    private double time;
}
