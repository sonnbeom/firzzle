package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : QuestionOptionDTO.java
 * @Description : 문제 보기 DTO (문제 옵션 정보 전달용)
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "문제 보기 정보")
public class OxQuizOptionDTO {

    @Schema(description = "보기 일련 번호", example = "1")
    private Long optionSeq;

    @Schema(description = "문제 일련 번호", example = "100")
    private Long questionSeq;

    @Schema(description = "보기 내용", example = "도메인 주도 설계는 객체지향을 기반으로 한다.")
    private String optionValue;
}
