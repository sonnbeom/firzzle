package com.firzzle.llm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.firzzle.llm.dto.ExamsDTO;

@Mapper
public interface ExamsMapper {
	/**
     * 시험 문제 리스트 저장
     *
     * @param exams 시험 문제 리스트
     * @return 저장된 개수
     */
    int insertExamList(List<ExamsDTO> exams);

    /**
     * 콘텐츠 기준 전체 시험 문제 개수 조회
     *
     * @param contentSeq 콘텐츠 번호
     * @return 전체 문제 수
     */
    int selectTotalExamCount(@Param("contentSeq") Long contentSeq);

    /**
     * 사용자가 푼 문제 수 조회
     *
     * @param contentSeq 콘텐츠 번호
     * @param userSeq 사용자 번호
     * @return 사용자 답변 수
     */
    int selectAnsweredExamCount(
    		@Param("contentSeq") Long contentSeq,
            @Param("userSeq") Long userSeq
    );

    /**
     * 다음으로 풀어야 할 문제 조회 (아직 답변하지 않은 문제 중 가장 앞에 있는 문제)
     *
     * @param contentSeq 콘텐츠 번호
     * @param userSeq 사용자 번호
     * @return 다음 문제 정보 (질문, 총 개수, 현재 인덱스)
     */
    // 다음 문제 조회 (answeredCount + 1 인덱스 기준)
    ExamsDTO selectNextExamQuestion(
    		@Param("contentSeq") Long contentSeq,
    		@Param("nextIndex") int nextIndex
    );	
}
