package com.firzzle.stt.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : ImageREquestDTO.java
 * @Description : 영상 요약의 섹션 단위 데이터 DTO (타임라인 기반 요약 등에서 활용됨)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Schema(description = "유튜브 STT 요청 DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestDTO {
    private String url;
    private List<String> timelines; // "00:01:10" 형식
}

