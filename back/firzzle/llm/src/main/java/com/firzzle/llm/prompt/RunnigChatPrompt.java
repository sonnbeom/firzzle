package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

@Component
public class RunnigChatPrompt {

	public String createPrompt(String question, String recentMessages, String retrievedContext) {
	    return String.format("""
	        [사용자 질문]
	        %s
	        
	        [스크립트 내용]
	        %s

	        [이전 대화 내용]
	        %s
	        """, retrievedContext, recentMessages, question);
	}
    public String createInstruction() {
        return """
        당신은 학생을 도와주는 똑똑한 AI 튜터입니다.
        주어진 영상 스크립트를 기반으로 질문에 답변하세요.

        [제약 조건]
        - 답변은 반드시 제공된 스크립트 내용에 기반해야 합니다.
        - 만약 스크립트에 관련 정보가 없으면 "해당 내용은 영상에서 언급되지 않았어요."라고 답변하세요.
        - 이해를 돕기 위해 예시나 비유가 있다면 간단히 포함하세요.
        - 학습용 챗봇이므로 너무 딱딱하거나 무성의한 답변은 피하세요.
        - 설명이 길어질 경우, 핵심을 먼저 말하고 상세 설명은 뒤에 추가하세요.
        """;
    }
}
