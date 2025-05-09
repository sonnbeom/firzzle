package com.firzzle.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : QuizSubmissionResponseDTO.java
 * @Description : 퀴즈 제출 결과 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResponseDTO {
    /**
     * 제출 정보
     */
    private SubmissionDTO submission;

    /**
     * 문제별 결과 목록
     */
    private List<QuestionResultDTO> questionResults;

    /**
     * @Class Name : SubmissionDTO
     * @Description : 제출 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionDTO {
        /**
         * 제출 일련번호
         */
        private Long seq;

        /**
         * 콘텐츠 일련번호
         */
        private Long contentSeq;

        /**
         * 정답 개수
         */
        private Integer correctAnswers;

        /**
         * 총 문제 개수
         */
        private Integer totalQuestions;

        /**
         * 정답률 (%)
         */
        private Float scorePercentage;

        /**
         * 제출 일시 (YYYY-MM-DD HH:MM:SS 형식)
         */
        private String indate;
    }

    /**
     * @Class Name : QuestionResultDTO
     * @Description : 문제별 결과 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultDTO {
        /**
         * 문제 일련번호
         */
        private Long questionSeq;

        /**
         * 문제 내용
         */
        private String question;

        /**
         * 선택한 답변
         */
        private String selectedAnswer;

        /**
         * 정답
         */
        private String correctAnswer;

        /**
         * 정답 여부
         */
        private Boolean isCorrect;

        /**
         * 문제 해설
         */
        private String explanation;
    }
}