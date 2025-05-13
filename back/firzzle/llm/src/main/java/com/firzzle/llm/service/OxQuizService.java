package com.firzzle.llm.service;

import com.firzzle.llm.dto.OxQuizDTO;
import com.firzzle.llm.mapper.OxQuizMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OxQuizService {

    private final OxQuizMapper oxQuizMapper;

    /**
     * OX 퀴즈 리스트를 DB에 저장
     * @param contentSeq 해당 콘텐츠 번호
     * @param oxQuizList 저장할 OX 퀴즈 리스트
     */
    public void saveOxQuizzes(Long contentSeq, List<OxQuizDTO> oxQuizList) {
        if (oxQuizList == null || oxQuizList.isEmpty()) return;

        List<OxQuizDTO> questions = oxQuizList.stream()
            .map(ox -> {
                ox.setContentSeq(contentSeq);
                ox.setType("OX");
                ox.setDeleteYn("N");
                return ox;
            })
            .collect(Collectors.toList());

        oxQuizMapper.insertQuestions(questions);
        log.info("✅ OX 퀴즈 {}개 저장 완료", questions.size());
    }
}
