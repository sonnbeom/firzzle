package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ContentRequestDTO.java
 * @Description : 콘텐츠 등록 요청 DTO
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 등록 요청 정보")
public class ContentRequestDTO {

    @NotBlank(message = "YouTube URL은 필수 입력값입니다.")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11}).*$",
            message = "올바른 YouTube URL 형식이 아닙니다.")
    @Schema(description = "YouTube 동영상 URL", required = true, example = "https://www.youtube.com/watch?v=xxxxxxxxxxx")
    private String youtubeUrl;

    @Size(max = 255, message = "제목은 최대 255자까지 입력 가능합니다.")
    @Schema(description = "콘텐츠 제목", required = true, example = "영어 회화 학습 - 기초 1편")
    private String title;

    @Size(max = 1000, message = "설명은 최대 1000자까지 입력 가능합니다.")
    @Schema(description = "콘텐츠 설명", example = "영어 회화 기초 학습을 위한 강의 영상입니다.")
    private String description;

    @Schema(description = "콘텐츠 카테고리", example = "EDUCATION", defaultValue = "EDUCATION")
    private String category;

    @Schema(description = "태그 (쉼표로 구분)", example = "영어,회화,학습,초급")
    private String tags;
}