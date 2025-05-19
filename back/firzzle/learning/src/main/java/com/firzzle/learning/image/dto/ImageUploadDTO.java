package com.firzzle.learning.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 업로드 요청 DTO")
public class ImageUploadDTO {

    @Schema(description = "이미지 카테고리", example = "profile")
    private String category;

    @Schema(description = "이미지 설명", example = "프로필 이미지 설명")
    private String description;

    @Schema(description = "이미지 공개 여부", example = "true")
    private Boolean isPublic;
}