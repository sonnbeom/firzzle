package com.firzzle.llm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firzzle.llm.dto.ExamsDTO;
import com.firzzle.llm.mapper.ExamsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamsService {

    private final ExamsMapper examsMapper;

    /**
     * AI 시험 문제 리스트를 DB에 저장
     * @param contentSeq 콘텐츠 번호
     * @param examList 저장할 시험 문제 리스트
     */
    @Transactional
    public void saveExams(Long contentSeq, List<ExamsDTO> examList) {
        if (examList == null || examList.isEmpty()) return;

        int index = 1;
        for (ExamsDTO exam : examList) {
            exam.setContentSeq(contentSeq);
            exam.setQuestionIndex(index++);
        }

        examsMapper.insertExamList(examList); // 실제 insert 수행

        log.info("✅ 시험 문제 {}개 저장 완료", examList.size());
    }
}
