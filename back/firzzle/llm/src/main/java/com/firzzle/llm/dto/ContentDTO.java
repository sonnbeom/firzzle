package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : ContentDTO.java
 * @Description : 콘텐츠 정보 DTO (MyBatis 매핑 및 API 응답용 DTO)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "콘텐츠 정보")
public class ContentDTO {

    @Schema(description = "콘텐츠 일련 번호", example = "1001")
    private Long contentSeq;

    @Schema(description = "유튜브 영상 ID", example = "eDDG-GWx6FM")
    private String videoId;

    @Schema(description = "영상 URL", example = "https://www.youtube.com/watch?v=eDDG-GWx6FM")
    private String url;

    @Schema(description = "영상 제목", example = "DDD 제대로 이해하기")
    private String title;

    @Schema(description = "영상 설명", example = "도메인 주도 설계(DDD)에 대한 소개 영상입니다.")
    private String description;

    @Schema(description = "카테고리", example = "소프트웨어 설계")
    private String category;

    @Schema(description = "썸네일 URL", example = "https://i.ytimg.com/vi/eDDG-GWx6FM/hqdefault.jpg")
    private String thumbnailUrl;

    @Schema(description = "영상 길이 (초)", example = "720")
    private Long duration;

    @Schema(description = "처리 상태", example = "COMPLETED")
    private String processStatus;

    @Schema(description = "등록 일시 (YYYYMMDDHHMMSS)", example = "20250515104500")
    private String indate;

    @Schema(description = "처리 완료 일시 (YYYYMMDDHHMMSS)", example = "20250515105030")
    private String completedAt;

    @Schema(description = "삭제 여부", example = "N")
    private String deleteYn;

    @Schema(description = "태그 목록 (쉼표로 구분)", example = "DDD,설계,도메인")
    private String tags;
}
