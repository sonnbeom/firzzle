package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.OxQuizDTO;
import com.firzzle.llm.dto.OxQuizOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OxQuizMapper {

    /**
     * 문제 1개 저장 (INSERT)
     * - question_seq는 AUTO_INCREMENT이므로, useGeneratedKeys로 반환 받음
     */
    int insertQuestion(OxQuizDTO question);

    /**
     * 보기 여러 개 저장
     * - question_seq를 포함하여 보기를 여러 개 저장
     */
    int insertQuestionOptions(@Param("options") List<OxQuizOptionDTO> options);

}
