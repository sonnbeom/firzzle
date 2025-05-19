package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @Class Name : RecommendContentDTO.java
 * @Description : 추천 콘텐츠 DTO (Qdrant 유사도 기반 추천 결과용 + MyBatis/REST 응답용 통합 구조)
 *                기존 콘텐츠 정보 + score, matchedTags 필드 포함
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "추천 콘텐츠 정보 + 유사도 정보 DTO")
public class RecommendContentDTO {

    @Schema(description = "콘텐츠 일련번호")
    private long contentSeq;

    @Schema(description = "콘텐츠 제목")
    private String title;

    @Schema(description = "콘텐츠 설명")
    private String description;

    @Schema(description = "콘텐츠 타입 (예: 교육/IT)")
    private String contentType;

    @Schema(description = "YouTube videoId")
    private String videoId;

    @Schema(description = "YouTube URL")
    private String url;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "재생 시간 (초)")
    private int duration;

    @Schema(description = "태그 목록 (쉼표로 구분된 문자열)", example = "딥러닝, 파이썬, 신경망")
    private String tags; // 또는 List<String> 로 변경 가능

    @Schema(description = "처리 상태 코드", example = "C")
    private String processStatus;

    @Schema(description = "AI 분석 데이터 (있을 경우)")
    private Object analysisData;

    @Schema(description = "자막 데이터 (있을 경우)")
    private Object transcript;

    @Schema(description = "등록일시", example = "2025-05-01 12:00:00")
    private String indate;

    @Schema(description = "처리 완료 일시", example = "2025-05-01 12:50:00")
    private String completedAt;

    @Schema(description = "삭제 여부", example = "N")
    private String deleteYn;

    @Schema(description = "처리 상태 텍스트", example = "완료")
    private String processStatusText;

    @Schema(description = "포맷된 재생 시간", example = "01:00:00")
    private String formattedDuration;
}
