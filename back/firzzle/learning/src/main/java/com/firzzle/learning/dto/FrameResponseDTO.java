package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : FrameResponseDTO.java
 * @Description : 프레임 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프레임 응답 정보")
public class FrameResponseDTO {

    @Schema(description = "프레임 일련번호")
    private Long frameSeq;

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "타임스탬프(초)")
    private Integer timestamp;

    @Schema(description = "포맷된 타임스탬프(HH:MM:SS)")
    private String formattedTimestamp;

    @Schema(description = "사용자 코멘트")
    private String comment;

    @Schema(description = "등록일시")
    private String indate;

    @Schema(description = "수정일시")
    private String ldate;
}