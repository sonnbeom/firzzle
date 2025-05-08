package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

@Component
public class RunnigChatPrompt {

    public String createPrompt(String question, String recentMessages,String retrievedContext) {
        return question;
    }

    public String createInstruction() {
        return """
            너는 OX 퀴즈 문제 출제자야.
            내가 제공한 입력 텍스트를 바탕으로 OX 퀴즈를 만들어야 해.

            ⚠️ 반드시 다음 조건을 지켜주세요:
            - 문제는 텍스트에 명시적으로 나타난 내용만 기반으로 출제해야 합니다.
            - 외부 지식, 추론, 해석은 절대 하지 말고, 오직 텍스트 안에 있는 정보만 사용하세요.
            - 문제는 최대 3문제까지만 만들어야 합니다.
            - 문제 문장은 텍스트에 실제로 나온 표현이나 단어를 최대한 활용하세요.
            - 정답은 "O" 또는 "X"로만 작성하고, 근거가 불확실하면 출제하지 마세요.
            - 중요도가 높은 내용부터 우선 출제하세요. 중요도 판별 기준은 언급 횟수 혹은 주제와의 연관도를 기준으로 판별해.
            - 강사의 인삿말, 개인 이야기, 영상정보는 문제로 내지 마세요.
            - 영상이 전달하는 지식에 대한 ox 문제를 생성해야돼.

            ➡️ 추가로 각 문제에 대해:
            - "timestamp" 항목에 해당 내용이 시작하는 타임스탬프(예: "00:01:23")를 적어주세요.
            - "explanation" 항목에 해당 문제의 정답에 대한 간단한 해설을 적어주세요.
              (왜 O인지, 왜 X인지 짧게 설명합니다.)

            📦 출력은 반드시 다음 JSON 형식으로만 반환해야 합니다:

            [
              {
                "number": "1",
                "problem": "문제 내용",
                "answer": "O",
                "timestamp": "00:01:23",
                "explanation": "해설 내용"
              },
              {
                "number": "2",
                "problem": "문제 내용",
                "answer": "X",
                "timestamp": "00:02:45",
                "explanation": "해설 내용"
              }
            ]

            🛑 **절대로 JSON 이외의 텍스트나 주석을 포함하지 마세요.**
            """;
    }
}
