package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ExamhistoryResponseDTO.java
 * @Description : 콘텐츠 정보 DTO (MyBatis 매핑 및 API 응답용 DTO)
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "시험 응답 기록 조회")
public class ExamHistoryResponseDTO {
    @Schema(description = "본문 내용", example = "톨킨은 판타지 장르를 대중화한 대표적인 작가입니다.")
    private String chatText;

    @Schema(description = "작성 시각 (YYYYMMDDHHMMSS)", example = "20250518205127")
    private String indate;

    @Schema(description = "유형 (0: 질문, 1: 답변, 2: 해설)", example = "1")
    private int type;
}