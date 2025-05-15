package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Class Name : SectionDTO.java
 * @Description : 영상 요약의 섹션 단위 데이터 DTO (타임라인 기반 요약 등에서 활용됨)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Schema(description = "요약 섹션 정보 DTO")
public class SectionDTO {

    @Schema(description = "섹션 일련 번호", example = "501")
    private long sectionSeq;

    @Schema(description = "요약 일련 번호 (summary_seq)", example = "3001", required = true)
    private long summarySeq;

    @Schema(description = "섹션 제목", example = "DDD의 핵심 개념")
    private String title;

    @Schema(description = "섹션 시작 시간 (초 단위)", example = "120")
    private int startTime;

    @Schema(description = "섹션 상세 요약 내용", example = "DDD는 복잡한 도메인을 설계하기 위한 전략이며...")
    private String details;
}
