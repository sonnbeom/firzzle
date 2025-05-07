package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : SnapReviewShareResponseDTO.java
 * @Description : 스냅리뷰 공유 코드 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스냅리뷰 공유 코드 응답 정보")
public class SnapReviewShareResponseDTO {

    @Schema(description = "공유 코드")
    private String shareCode;

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "공유 URL")
    private String shareUrl;

    @Schema(description = "등록일시")
    private String indate;
}