package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class OxQuizDTO {
    private Long questionSeq;        // 문제 일련 번호 (PK, auto_increment이면 insert 시 생략)
    private Long contentSeq;         // 콘텐츠 일련 번호 (필수)
    private String question;         // 문제 내용
    private String type;             // 문제 유형 (예: "OX")
    private String correctAnswer;    // 정답 ("O" 또는 "X")
    private String explanation;      // 해설
    private Integer startTime;       // 시작 시간 (초, nullable)
    private String deleteYn = "N";   // 삭제 여부 기본값 "N"
}
