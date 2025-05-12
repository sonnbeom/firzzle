package com.firzzle.learning.ai.dto;

import com.firzzle.common.request.CursorRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채팅 내역 조회를 위한 커서 기반 페이징 요청 DTO
 */
@Schema(description = "채팅 내역 조회를 위한 커서 기반 페이징 요청 정보")
@Getter
@Setter
@NoArgsConstructor
public class ChatSearchDTO extends CursorRequestDTO {

    @Schema(description = "정렬 기준 필드명", example = "chat_seq", allowableValues = {"chat_seq", "indate"}, defaultValue = "chat_seq")
    @Override
    public String getOrderBy() {
        String orderBy = super.getOrderBy();
        // 기본값 또는 유효성 검증
        if (orderBy == null || orderBy.isEmpty() ||
                (!orderBy.equals("chat_seq") && !orderBy.equals("indate"))) {
            return "chat_seq";
        }
        return orderBy;
    }
}