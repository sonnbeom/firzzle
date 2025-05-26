package com.firzzle.llm.service;

import com.firzzle.llm.dto.OxQuizDTO;
import com.firzzle.llm.dto.OxQuizOptionDTO;
import com.firzzle.llm.mapper.OxQuizMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

        // 🔹 분산된 3개 선택
        List<OxQuizDTO> selected = selectEvenly(oxQuizList, 3);

        for (OxQuizDTO ox : selected) {
            ox.setContentSeq(contentSeq);
            ox.setType("OX");
            ox.setDeleteYn("N");

            oxQuizMapper.insertQuestion(ox);

            List<OxQuizOptionDTO> options = List.of(
                OxQuizOptionDTO.builder().questionSeq(ox.getQuestionSeq()).optionValue("O").build(),
                OxQuizOptionDTO.builder().questionSeq(ox.getQuestionSeq()).optionValue("X").build()
            );
            oxQuizMapper.insertQuestionOptions(options);
        }

        log.info("✅ 분산된 OX 퀴즈 3개 저장 완료: {}", selected.size());
    }

    
    private List<OxQuizDTO> selectEvenly(List<OxQuizDTO> quizList, int count) {
        int size = quizList.size();
        if (size <= count) return quizList; // 3개 이하면 그대로 반환

        List<OxQuizDTO> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = (int) Math.round((double) i * (size - 1) / (count - 1));
            result.add(quizList.get(index));
        }
        return result;
    }

}
