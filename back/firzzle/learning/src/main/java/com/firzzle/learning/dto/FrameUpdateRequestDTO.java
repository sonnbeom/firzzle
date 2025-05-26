package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : FrameUpdateRequestDTO.java
 * @Description : 프레임 정보 수정 요청 DTO
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프레임 정보 수정 요청")
public class FrameUpdateRequestDTO {

    @Schema(description = "프레임 정보 목록")
    @NotEmpty(message = "프레임 정보는 최소 1개 이상 필요합니다.")
    private List<@Valid FrameInfo> frames;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프레임 정보")
    public static class FrameInfo {

        @Schema(description = "프레임 일련번호", required = true)
        @NotNull(message = "프레임 일련번호는 필수입니다.")
        private Long frameSeq;

        @Schema(description = "코멘트", required = false)
        private String comment;
    }
}