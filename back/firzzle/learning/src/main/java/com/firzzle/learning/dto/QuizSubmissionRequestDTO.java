package com.firzzle.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @Class Name : QuizSubmissionRequestDTO.java
 * @Description : 퀴즈 제출 요청 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionRequestDTO {
    /**
     * 답변 목록
     */
    @NotEmpty(message = "답변 목록은 비어있을 수 없습니다.")
    private List<AnswerDTO> answers;

    /**
     * @Class Name : AnswerDTO
     * @Description : 퀴즈 답변 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDTO {
        /**
         * 문제 일련번호
         */
        @NotNull(message = "문제 일련번호는 필수 항목입니다.")
        private Long questionSeq;

        /**
         * 선택한 답변
         */
        @NotNull(message = "선택한 답변은 필수 항목입니다.")
        private String selectedAnswer;
    }
}