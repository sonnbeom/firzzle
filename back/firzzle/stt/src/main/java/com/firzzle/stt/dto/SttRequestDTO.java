package com.firzzle.stt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "유튜브 STT 요청 DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SttRequestDTO {

    @Schema(description = "유튜브 영상 URL", example = "https://www.youtube.com/watch?v=abcdef")
    private String url;

    @Schema(description = "내부 사용자 고유 식별자", example = "1001")
    private Long userSeq;
}
