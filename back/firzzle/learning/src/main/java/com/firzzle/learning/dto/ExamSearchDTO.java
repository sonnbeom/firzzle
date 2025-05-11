package com.firzzle.learning.dto;

import com.firzzle.common.request.CursorRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 시험 내역 조회를 위한 커서 기반 페이징 요청 DTO
 */
@Schema(description = "시험 내역 조회를 위한 커서 기반 페이징 요청 정보")
@Getter
@Setter
@NoArgsConstructor
public class ExamSearchDTO extends CursorRequestDTO {

    @Schema(description = "정렬 기준 필드명", example = "exam_seq", allowableValues = {"exam_seq", "indate", "question_number"}, defaultValue = "exam_seq")
    @Override
    public String getOrderBy() {
        String orderBy = super.getOrderBy();
        // 기본값 또는 유효성 검증
        if (orderBy == null || orderBy.isEmpty() ||
                (!orderBy.equals("exam_seq") && !orderBy.equals("indate") && !orderBy.equals("question_number"))) {
            return "exam_seq";
        }
        return orderBy;
    }
}