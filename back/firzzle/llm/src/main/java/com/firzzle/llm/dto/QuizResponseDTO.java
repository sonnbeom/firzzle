package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : QuizResponseDTO.java
 * @Description : 퀴즈 조회 결과 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDTO {
    /**
     * 콘텐츠 정보
     */
    private ContentDTO content;

    /**
     * 제출 정보
     */
    private SubmissionDTO submission;

    /**
     * @Class Name : ContentDTO
     * @Description : 콘텐츠 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDTO {
        /**
         * 콘텐츠 일련번호
         */
        private Long contentSeq;

        /**
         * 퀴즈 문제 목록
         */
        private List<QuestionDTO> questions;
    }

    /**
     * @Class Name : QuestionDTO
     * @Description : 퀴즈 문제 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        /**
         * 문제 일련번호
         */
        private Long questionSeq;

        /**
         * 문제 내용
         */
        private String text;

        /**
         * 문제 유형 (multiple_choice, short_answer 등)
         */
        private String type;

        @Schema(description = "타임스탬프(초)")
        private Integer timestamp;

        @Schema(description = "포맷된 타임스탬프(HH:MM:SS)")
        private String formattedTimestamp;

        /**
         * 문제 보기 목록
         */
        private List<OptionDTO> options;

        /**
         * 사용자 답변 정보
         */
        private UserAnswerDTO userAnswer;
    }

    /**
     * @Class Name : OptionDTO
     * @Description : 퀴즈 문제 보기 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        /**
         * 보기 일련번호
         */
        private Long optionSeq;

        /**
         * 보기 내용
         */
        private String text;
    }

    /**
     * @Class Name : UserAnswerDTO
     * @Description : 사용자 답변 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnswerDTO {
        /**
         * 사용자가 선택한 답변 일련번호
         */
        private Long selectedOptionSeq;

        /**
         * 정답 여부
         */
        private Boolean isCorrect;

        /**
         * 문제 해설
         */
        private String explanation;
    }

    /**
     * @Class Name : SubmissionDTO
     * @Description : 퀴즈 제출 정보 데이터 전송 객체
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionDTO {
        /**
         * 제출 일련번호
         */
        private Long submissionSeq;

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
}