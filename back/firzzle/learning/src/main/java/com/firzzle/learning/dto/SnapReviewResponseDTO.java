package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스냅리뷰 응답 정보")
public class SnapReviewResponseDTO {

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "콘텐츠 제목")
    private String contentTitle;

    @Schema(description = "콘텐츠 썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "등록일시")
    private String indate;

    @Schema(description = "프레임 정보 목록")
    private List<FrameDTO> frames;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프레임 정보")
    public static class FrameDTO {

        @Schema(description = "프레임 일련번호")
        private Long frameSeq;

        @Schema(description = "이미지 URL")
        private String imageUrl;

        @Schema(description = "타임스탬프(초)")
        private Integer timestamp;

        @Schema(description = "포맷된 타임스탬프(HH:MM:SS)")
        private String formattedTimestamp;

        @Schema(description = "의견")
        private String comment;
    }
}