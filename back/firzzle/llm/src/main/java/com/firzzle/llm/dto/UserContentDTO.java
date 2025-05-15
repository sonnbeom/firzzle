package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Class Name : UserContentDTO.java
 * @Description : 사용자-콘텐츠 매핑 정보 DTO (진행률 및 접근 이력 포함)
 * author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Schema(description = "사용자-콘텐츠 매핑 정보 DTO")
public class UserContentDTO {

    @Schema(description = "사용자 콘텐츠 일련 번호 (PK)", example = "501")
    private Long userContentSeq;

    @Schema(description = "내부 사용자 고유 식별자", example = "12", required = true)
    private Long userSeq;

    @Schema(description = "콘텐츠 일련 번호", example = "1001", required = true)
    private Long contentSeq;

    @Schema(description = "콘텐츠 진행률 (0~100)", example = "85")
    private int progress;

    @Schema(description = "마지막 접근 시간 (YYYYMMDDHHMMSS)", example = "20250515143000")
    private String lastAccessedAt;

    @Schema(description = "등록 일시 (YYYYMMDDHHMMSS)", example = "20250515142500")
    private String indate;
}
