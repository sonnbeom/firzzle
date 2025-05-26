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
@Schema(description = "용어 정의 정보")
public class TermDefinitionDTO {

    @Schema(description = "용어")
    private String term;

    @Schema(description = "정의")
    private String definition;

    @Schema(description = "콘텐츠 내 출현 시간(초)")
    private List<Integer> occurrenceTimes;

    @Schema(description = "관련 주제")
    private List<String> relatedTopics;

    @Schema(description = "출처")
    private String source;
}
