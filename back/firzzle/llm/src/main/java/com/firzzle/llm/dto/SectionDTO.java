package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : SectionDTO.java
 * @Description : 콘텐츠 요약 섹션 DTO
 * @author Firzzle
 * @since 2025. 5. 7.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 요약 섹션 정보")
public class SectionDTO {

    @Schema(description = "섹션 일련번호")
    private Long sectionSeq;

    @Schema(description = "섹션 제목")
    private String title;

    @Schema(description = "섹션 내용 요약")
    private String content;

    @Schema(description = "시작 시간(초)")
    private Integer startTime;

    @Schema(description = "섹션 상세 내용 (JSON 배열)")
    private List<String> details;
}