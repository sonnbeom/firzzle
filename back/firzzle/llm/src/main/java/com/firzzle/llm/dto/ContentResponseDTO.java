package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Class Name : ContentResponseDTO.java
 * @Description : 콘텐츠 응답 DTO
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 응답 정보")
public class ContentResponseDTO {

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "콘텐츠 제목")
    private String title;

    @Schema(description = "콘텐츠 설명")
    private String description;

    @Schema(description = "콘텐츠 카테고리")
    private String contentType;

    @Schema(description = "YouTube 동영상 ID")
    private String videoId;

    @Schema(description = "YouTube 동영상 URL")
    private String url;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "콘텐츠 길이 (초)")
    private Integer duration;

    @Schema(description = "태그 (쉼표로 구분)")
    private String tags;

    @Schema(description = "분석 상태 (Q: 대기중, P: 분석중, C: 완료, F: 실패)")
    private String processStatus;

    @Schema(description = "분석 결과 데이터 (JSON 형식)")
    private String analysisData;

    @Schema(description = "스크립트 텍스트")
    private String transcript;

    @Schema(description = "등록일시")
    private String indate;

    @Schema(description = "처리 완료 일시")
    private String completedAt;

    @Schema(description = "삭제 여부")
    private String deleteYn;

    /**
     * 처리 상태 코드(Q, P, C, F)를 읽기 쉬운 텍스트로 변환
     *
     * @return String - 처리 상태 텍스트
     */
    @Schema(description = "처리 상태 텍스트")
    public String getProcessStatusText() {
        if (this.processStatus == null) {
            return "알 수 없음";
        }

        switch (this.processStatus) {
            case "Q":
                return "대기중";
            case "P":
                return "분석중";
            case "C":
                return "완료";
            case "F":
                return "실패";
            default:
                return "알 수 없음";
        }
    }

    /**
     * 영상 길이를 HH:MM:SS 형식으로 변환
     *
     * @return String - 포맷된 영상 길이
     */
    @Schema(description = "포맷된 영상 길이 (HH:MM:SS)")
    public String getFormattedDuration() {
        if (this.duration == null) {
            return "00:00:00";
        }

        int hours = this.duration / 3600;
        int minutes = (this.duration % 3600) / 60;
        int seconds = this.duration % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}