package com.firzzle.llm.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Class Name : OxQuizDTO.java
 * @Description : OX 퀴즈 문제 데이터 전송 객체 (LLM 생성 퀴즈용 DTO 포함 조회/등록/수정용으로 사용 가능)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Schema(description = "OX 퀴즈 문제 DTO")
public class OxQuizDTO {

    @Schema(description = "문제 일련 번호 (PK, auto_increment)", example = "101")
    private Long questionSeq;

    @Schema(description = "콘텐츠 일련 번호 (필수)", example = "1001", required = true)
    private Long contentSeq;

    @Schema(description = "OX 문제 내용", example = "DDD는 도메인 주도 설계이다.")
    private String question;

    @Schema(description = "문제 유형", example = "OX")
    private String type;

    @Schema(description = "정답 (O 또는 X)", example = "O")
    private String correctAnswer;

    @Schema(description = "해설", example = "DDD는 Domain-Driven Design의 약자로 도메인 모델을 중심으로 애플리케이션을 설계하는 방법론입니다.")
    private String explanation;

    @Schema(description = "문제가 등장하는 시작 시간 (초)", example = "85")
    private Integer startTime;

    @Schema(description = "삭제 여부 (기본값 N)", example = "N")
    private String deleteYn = "N";
    
    private List<OxQuizOptionDTO> options; // 보기들
}
