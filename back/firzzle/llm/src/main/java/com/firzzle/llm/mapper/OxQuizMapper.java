package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.OxQuizDTO;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OxQuizMapper {
	void insertQuestions(List<OxQuizDTO> questions);
}
