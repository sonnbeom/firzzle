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
}

