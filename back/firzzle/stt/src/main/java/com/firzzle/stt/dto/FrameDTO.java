package com.firzzle.stt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : FrameDTO.java
 * @Description : 콘텐츠 프레임 정보 전송 객체
 * @author Firzzle
 * @since 2025. 5. 20.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "영상에서 추출한 프레임 이미지 정보")
public class FrameDTO {

    @Schema(description = "프레임 일련 번호", example = "1")
    private Long frameSeq;

    @Schema(description = "프레임 이미지 URL", example = "https://cdn.example.com/images/frame1.jpg")
    private String imageUrl;

    @Schema(description = "프레임 타임스탬프 (초)", example = "70")
    private Integer timestamp;

    @Schema(description = "수정일시 (YYYYMMDDHHMMSS)", example = "20250520101530")
    private String ldate;

    @Schema(description = "콘텐츠 일련 번호", example = "1001")
    private Long contentSeq;

    @Schema(description = "등록일시 (YYYYMMDDHHMMSS)", example = "20250520101010")
    private String indate;
}
