package com.firzzle.llm.service;

import com.firzzle.llm.dto.OxQuizDTO;
import com.firzzle.llm.dto.OxQuizOptionDTO;
import com.firzzle.llm.mapper.OxQuizMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void saveOxQuizzes(Long contentSeq, List<OxQuizDTO> oxQuizList) {
        if (oxQuizList == null || oxQuizList.isEmpty()) return;

        for (OxQuizDTO ox : oxQuizList) {
            ox.setContentSeq(contentSeq);
            ox.setType("OX");
            ox.setDeleteYn("N");

            // 1. 문제 등록 → examSeq 채워짐
            oxQuizMapper.insertQuestion(ox);

            // 2. 보기 생성 (O, X)
            List<OxQuizOptionDTO> options = List.of(
                OxQuizOptionDTO.builder()
                    .questionSeq(ox.getQuestionSeq())
                    .optionValue("O")
                    .build(),
               OxQuizOptionDTO.builder()
                    .questionSeq(ox.getQuestionSeq())
                    .optionValue("X")
                    .build()
            );

            // 3. 보기 저장
            oxQuizMapper.insertQuestionOptions(options);
        }

        log.info("✅ OX 퀴즈 {}개 저장 완료", oxQuizList.size());
    }

}
