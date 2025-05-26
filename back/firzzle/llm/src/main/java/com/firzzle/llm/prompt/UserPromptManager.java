package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

@Component
public class UserPromptManager {

    public String getlearningChatUserPrompt(String question, String context, String previous) {
        return String.format("""
            사용자 질문과 관련된 영상 문맥입니다.

            [문맥]
            %s

            [질문]
            %s
            
            [이전 대화]
            %s
        """, context, question, previous);
    }

    public String getSummaryUserPrompt(String transcript) {
        return transcript;
    }

    public String getTimelineUserPrompt(String script) {
        return script;
    }

    public String getExamUserPrompt( String userAnswer, String modelAnswer, String referenceText) {
        return String.format("""
            [사용자 답변]
            %s

            [모델 답변]
            %s

            [참고 텍스트]
            %s

            위 내용을 기반으로, 사용자 답변에 대한 평가 및 해설을 300자 이내로 작성하세요.
        """, userAnswer, modelAnswer, referenceText);
    }
}

