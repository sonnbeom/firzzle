package com.firzzle.learning.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 응답 DTO")
public class ImageResponseDTO {

    @Schema(description = "이미지 일련번호", example = "1")
    private Long imageSeq;

    @Schema(description = "이미지 파일명", example = "profile_123456789_image.jpg")
    private String filename;

    @Schema(description = "이미지 URL", example = "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profile/profile_123456789_image.jpg")
    private String imageUrl;

    @Schema(description = "이미지 카테고리", example = "profile")
    private String category;

    @Schema(description = "이미지 설명", example = "프로필 이미지 설명")
    private String description;

    @Schema(description = "이미지 공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "등록일시", example = "2025-05-15 14:30:25")
    private String indate;
}