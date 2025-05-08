package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

@Component
public class QuizPrompt {

	public String createPrompt(String question, String recentMessages, String retrievedContext) {
	    return """
	        🔁 사용자와 AI 사이의 최근 대화 내용:
	        %s

	        📚 질문과 관련된 참고 문서:
	        %s

	        💬 이제 위의 문맥을 참고하여 다음 질문에 정확하고 간결하게 답변해주세요:

	        질문: %s
	        """.formatted(recentMessages, retrievedContext, question);
	}

	public String createInstruction() {
	    return """
	        당신은 강의 자막 데이터를 바탕으로 질문에 답변하는 전문가 AI 챗봇입니다.

	        💡 역할:
	        - 사용자의 질문에 대해 반드시 강의 내용에 기반해서만 답변하세요.
	        - 근거가 명확하지 않거나 강의 내용에 없는 정보는 유추하지 마세요.

	        🎯 답변 형식:
	        - 내용이 출처 문맥에 기반한 것임을 명확히 하세요.
	        - 가능하다면 관련 내용이 언급된 타임라인(예: "00:12:34")도 함께 제공하세요.

	        📌 주의사항:
	        - 너무 긴 답변을 피하고, 핵심을 명확하게 전달하세요.
	        - 정답이 확실하지 않다면 "강의에서 해당 내용은 언급되지 않았습니다"라고 답변하세요.

	        %s
	        """;
	}
}
