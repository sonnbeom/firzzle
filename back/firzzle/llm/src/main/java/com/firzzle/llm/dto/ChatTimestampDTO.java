package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : ChatTimestampDTO.java
 * @Description : 대화별 타임스탬프 데이터 전송 객체 (영상 구간 정보 등 포함 가능)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 챗봇 대화의 관련 타임스탬프 정보")
public class ChatTimestampDTO {

    @Schema(description = "타임스탬프 일련 번호", example = "5001")
    private Long timestampSeq;

    @Schema(description = "대화 일련 번호", example = "101")
    private Long chatSeq;

    @Schema(description = "시작 시간 (초)", example = "32")
    private Integer startTime;

    @Schema(description = "종료 시간 (초)", example = "78")
    private Integer endTime;

    @Schema(description = "타임스탬프 설명", example = "DDD에 대한 정의 설명 구간")
    private String description;
}