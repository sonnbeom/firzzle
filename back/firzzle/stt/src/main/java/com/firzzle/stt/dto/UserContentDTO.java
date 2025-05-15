package com.firzzle.stt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "사용자-콘텐츠 매핑 정보 DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContentDTO {

    @Schema(description = "사용자 콘텐츠 일련 번호")
    private Long userContentSeq;

    @Schema(description = "내부 사용자 고유 식별자")
    private Long userSeq;

    @Schema(description = "콘텐츠 일련 번호")
    private Long contentSeq;

    @Schema(description = "진행률 (0~100)")
    private int progress;

    @Schema(description = "마지막 접근 시간 (YYYYMMDDHHMMSS)")
    private String lastAccessedAt;

    @Schema(description = "등록 일시 (YYYYMMDDHHMMSS)")
    private String indate;
}