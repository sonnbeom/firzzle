package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 이미지 생성 API 요청 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageGenerationRequestDTO extends BaseGPTRequestDTO {

    @Schema(description = "이미지 생성을 위한 설명 텍스트", example = "A cat sitting on a beach")
    private String prompt;

    @Schema(description = "생성할 이미지 수", example = "1")
    private Integer n;

    @Schema(description = "이미지 크기", example = "1024x1024")
    private String size;
}