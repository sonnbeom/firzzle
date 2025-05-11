package com.firzzle.llm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firzzle.llm.dto.SummaryDTO;
import com.firzzle.llm.dto.SectionDTO;
import com.firzzle.llm.mapper.SummaryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummaryService {
	private final SummaryMapper summaryMapper;
	
    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);

    /**
     * 요약 및 섹션 정보를 DB에 저장
     * @param summary 요약 정보
     * @param sections 해당 요약에 속한 섹션들
     */
    @Transactional
    public void saveSummaryWithSections(SummaryDTO summary, List<SectionDTO> sections) {
        try {
            logger.info("📥 요약 저장 시작: contentSeq={}, level={}", summary.getContentSeq(), summary.getLevel());
            summaryMapper.insertSummary(summary); // summarySeq 자동 생성

            logger.info("📥 섹션 저장 시작: {}개", sections.size());

            // summarySeq를 모든 section에 주입
            for (SectionDTO section : sections) {
                section.setSummarySeq(summary.getSummarySeq());
            }

            summaryMapper.insertSections(sections); // ⬅️ 한 번에 삽입 (batch insert)

            logger.info("✅ 요약 및 섹션 저장 완료 (summarySeq={})", summary.getSummarySeq());

        } catch (Exception e) {
            logger.error("❌ 요약 및 섹션 저장 실패", e);
            throw new RuntimeException("요약 및 섹션 저장 중 오류 발생", e);
        }
    }
}
