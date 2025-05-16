package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ChatHistoryResponseDTO.java
 * @Description : 사용자-챗봇 대화 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자와 AI 챗봇 간의 대화 정보")
public class ChatHistoryResponseDTO {
    @Schema(description = "대화 일련 번호", example = "101")
    private Long chatSeq;

    @Schema(description = "대화 용", example = "DDD는 무슨 뜻인가요?")
    private String chatText;

    @Schema(description = "생성 일시 (YYYYMMDDHHMMSS)", example = "20250515103045")
    private String indate;
    
    @Schema(description = "텍스트 타입(1: gpt, 0: 사용자)", example = "0 or 1")
    private int type;
}
