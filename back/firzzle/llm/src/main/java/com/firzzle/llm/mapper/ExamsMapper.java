package com.firzzle.llm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.firzzle.llm.dto.ExamsDTO;

@Mapper
public interface ExamsMapper {
	int insertExamList(List<ExamsDTO> examList);
}
