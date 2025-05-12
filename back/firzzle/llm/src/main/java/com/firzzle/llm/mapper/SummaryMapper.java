package com.firzzle.llm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.firzzle.llm.dto.SummaryDTO;
import com.firzzle.llm.dto.SectionDTO;

@Mapper
public interface SummaryMapper {
    void insertSummary(SummaryDTO summary);
    void insertSections(List<SectionDTO> sectionList);
}
