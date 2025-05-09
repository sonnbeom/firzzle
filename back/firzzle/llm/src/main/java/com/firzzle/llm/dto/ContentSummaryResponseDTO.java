package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : ContentSummaryResponseDTO.java
 * @Description : 콘텐츠 요약 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 요약 정보 응답")
public class ContentSummaryResponseDTO {

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    // 쉬운 버전 요약 정보
    @Schema(description = "쉬운 요약 일련번호")
    private Long easySummarySeq;

    @Schema(description = "쉬운 요약 주제")
    private String easyMajorTopic;

    @Schema(description = "쉬운 난이도 섹션 목록")
    private List<SectionDTO> easySections;

    @Schema(description = "쉬운 요약 등록일시")
    private String easyIndate;

    // 어려운 버전 요약 정보
    @Schema(description = "어려운 요약 일련번호")
    private Long hardSummarySeq;

    @Schema(description = "어려운 요약 주제")
    private String hardMajorTopic;

    @Schema(description = "어려운 난이도 섹션 목록")
    private List<SectionDTO> hardSections;

    @Schema(description = "어려운 요약 등록일시")
    private String hardIndate;
}