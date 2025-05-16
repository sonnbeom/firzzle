package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Class Name : SummaryDTO.java
 * @Description : 콘텐츠 요약 메타 정보 DTO (난이도별 요약 구분 포함, 타임라인 요약과 연계 가능)
 * author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Schema(description = "요약 메타 정보 DTO")
public class SummaryDTO {

    @Schema(description = "요약 일련 번호 (PK)", example = "3001")
    private long summarySeq;

    @Schema(description = "콘텐츠 일련 번호 (FK)", example = "1001", required = true)
    private long contentSeq;

    @Schema(description = "요약 난이도 구분: 'E'(Easy), 'H'(High)", example = "E", allowableValues = {"E", "H"})
    private String level;

    @Schema(description = "요약 생성 일시 (형식: YYYYMMDDHHMMSS)", example = "20250515115030")
    private String indate;
}
