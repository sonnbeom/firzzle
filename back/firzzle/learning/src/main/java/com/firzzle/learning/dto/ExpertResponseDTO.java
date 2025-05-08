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
@Schema(description = "전문가 정보")
public class ExpertResponseDTO {

    @Schema(description = "전문가 일련번호")
    private Long expertSeq;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "직함")
    private String title;

    @Schema(description = "회사")
    private String company;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "LinkedIn URL")
    private String linkedinUrl;

    @Schema(description = "관련성 점수")
    private Float relevance;

    @Schema(description = "전문 분야 목록")
    private List<String> expertise;
}
