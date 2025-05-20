package com.firzzle.llm.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : SnapReviewRequestDTO.java
 * @Description : 스냅 리뷰 생송 요청 전송 
 * @author Firzzle
 * @since 2025. 5. 20.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스냅 리뷰 생성 요청")
public class SnapReviewRequestDTO {
	@Schema(description = "콘텐츠 일련벌호", example = "1")
	private Long ContentSeq;
	
	@Schema(description = "스냅 리뷰 타임라인인", example = "20250519165647")
	private List<String> Timeline;
}
