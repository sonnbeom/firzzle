package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.OxQuizDTO;
import com.firzzle.llm.dto.OxQuizOptionDTO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OxQuizMapper {
	  int insertQuestion(OxQuizDTO question); // 단일 insert, auto_key 받기
	   int insertQuestionOptions(@Param("options") List<OxQuizOptionDTO> options);
}
