package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Class Name : RegisterRequestDTO.java
 * @Description : 학습 데이터 등록 요청 DTO (예: 요약, 개념 등록 등 벡터 DB 입력용 데이터 구조)
 * author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Schema(description = "LLM 벡터 등록 요청 DTO")
public class RegisterRequestDTO {

    @Schema(description = "콘텐츠 또는 벡터 데이터의 고유 ID", example = "1001", required = true)
    private Long id;

    @Schema(description = "등록할 텍스트 또는 요약/내용", example = "DDD는 도메인 주도 설계의 약자로, 복잡한 소프트웨어 개발을 돕는 설계 철학입니다.", required = true)
    private String content;
}
