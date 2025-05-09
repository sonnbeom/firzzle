package com.firzzle.llm.prompt;

import org.springframework.stereotype.Component;

@Component
public class SummaryPrompt {    
    public String createInstruction() {
        return """
		Your Task
		Segment the given script into groups of approximately 15,000 bytes each. The goal is to maximize byte utilization without breaking contextually linked sentences or paragraphs unnaturally.
		
		Rules
		
		Primary goal: Utilize as much of the 15,000-byte limit as possible in each group.
		
		If contextually linked sentences exceed 15,000 bytes slightly, keep them together in one group.
		
		If contextually linked content far exceeds 15,000 bytes, start a new group even if the current group is underutilized.
		
		Do not break sentence or paragraph continuity unnecessarily.
		
		Output a JSON array of start times (in seconds), where each object represents the time extracted from the first line of each group.
		
		Input format
		
		Each line in the script starts with a time in seconds:

		[68] 그는 천천히 걸어 들어왔다.
		[72] 조용한 침묵이 흘렀다.
		...
		Output format
		[
		  { "time": 68 },
		  { "time": 312 },
		  { "time": 755 }
		]
		Only include real time values from the input (as integers).
		Ensure groups are balanced across the entire script and not skewed toward beginning or end.

        """;
    }

    public String createInstruction2() {
		return """
		당신은 주어진 영상 스크립트를 바탕으로 '학습자가 해당 학습 내용을 빠르게 파악하여 공부할 수 있는 학습 콘텐츠'를 자동으로 생성하는 교육 전문 AI입니다.
		주어진 작업 목표를 수행하세요.
        
		작업 목표:
		주어진 스크립트를 분석하여, 다음 항목을 생성하세요:
		
		---
		1. timelineStructured

		* 주어진 스크립트에서 '학습과 직접 관련이 있는 내용'을 중심으로 목차를 세세하게 나누어 정리하세요.
		* 각 소주제는 학습자가 학습 흐름을 잘 따라갈 수 있도록 작은 단위로 나누어야 합니다.
		* 주제 분할 시 다음 기준을 따르세요:
			* 주제가 바뀌거나 새로운 개념이 도입될 때마다 새로운 소주제로 시작
			* 미리보기 하이라이트 장면, 광고, 서론/도입부 등 학습과 직접적으로 관련이 없는 내용은 목차에서 제외
		* 각 소주제는 다음 항목으로 구성됩니다:

			* "title": 해당 구간의 핵심 소제목을 직관적이고 간결하게 작성
			* "time": 해당 소주제의 시작 시각 (예: "4620") → 반드시 초 단위 문자열 사용
			* "summary_Easy": 배경지식이 전혀 없는 사용자도 이해할 수 있도록 친절하고 상세하게 서술
            	- 어려운 용어는 구체적인 예시와 쉬운 설명을 포함
				- 초등학생 수준의 학습자도 이해할 수 있는 말투로 작성
			* "summary_High": 배경지식이 있는 사용자(대학생 또는 현업 종사자)를 위한 요약
				- 핵심만 간결하게 개조식으로 작성
				- 용어는 기본적으로 이해한다고 가정하고 설명

		
		---
		2. oxQuiz
		   - 각 소주제 별로 학습에 있어서 관건이 되는 내용 또는 학습자가 헷갈릴 만한 내용에 대한 OX 문제 1개를 만드세요.
		     - problem: 문제 내용
		     - answer: "O" 또는 "X"
		     - timestamp: 관련된 자막 시작 시각 (초 단위 문자열)
		     - explanation: 해당 정답의 근거를 논리적으로 설명(영상 내용을 인용해서)
		
		---
		3. descriptiveQuiz
		   - 각 소주제 별로 학습에 있어서 관건이 되는 내용 또는 학습자가 헷갈릴 만한 내용에 대한 서술형 문제 1개와 예시 정답을 작성하세요.
		     - question: 열린 질문 형태의 문제
		     - answer: 구체적이고 자연스러운 서술형 정답(영상 내용 인용해서)
		
		---
		출력은 각 소주제 별로 항목들을 하나의 JSON 객체로 묶어서 출력하세요:
		```json
        {
            {
              "title": "소주제 A",
              "time": "4620",
              "summary_Easy": "이중부호화 이론은 글과 그림을 함께 보면 더 잘 기억할 수 있다는 이론이에요. 예를 들어, 글로만 공부하는 것보다 그림과 함께 공부하면 더 쉽게 이해할 수 있어요.",
              "summary_High": "이중부호화 이론: 글과 시각적 이미지를 동시에 제시하면 학습 효과가 극대화됨.",
              "oxQuiz": {
					"problem": "첫사랑은 시간이 지나도 그 기억이 변하지 않는다.",
					"answer": "X",
					"timestamp": "4710",
					"explanation": "무두셀라 효과에 따르면, 첫사랑과 같은 강렬한 기억은 시간이 지날수록 더욱 이상화되거나 왜곡될 수 있음."
				},
                "descriptiveQuiz": {
					"question": "무두셀라 효과란 무엇이며, 첫사랑의 기억이 왜곡될 수 있는 이유는 무엇인가요?",
					"answer": "무두셀라 효과는 시간이 지날수록 기억이 과장되거나 이상화되는 현상을 말합니다. 첫사랑은 강렬한 감정으로 각인되기 때문에 시간이 지날수록 더 아름답거나 아련하게 기억될 수 있습니다."
          		}
            },
            {
              "title": "소주제 B",
              "time": "4700",
              "summary_Easy": "무두셀라 효과는 오래된 기억이 시간이 지날수록 더 강렬해지는 현상이에요. 예를 들어, 첫사랑은 시간이 지날수록 더 아름답게 기억될 수 있어요.",
              "summary_High": "무두셀라 효과: 시간이 지날수록 기억이 과장되거나 이상화되는 현상.",
              "oxQuiz": {
					"problem": "첫사랑은 시간이 지나도 그 기억이 변하지 않는다.",
					"answer": "X",
					"timestamp": "4710",
					"explanation": "무두셀라 효과에 따르면, 첫사랑과 같은 강렬한 기억은 시간이 지날수록 더욱 이상화되거나 왜곡될 수 있음."
				},
                "descriptiveQuiz": {
					"question": "무두셀라 효과란 무엇이며, 첫사랑의 기억이 왜곡될 수 있는 이유는 무엇인가요?",
					"answer": "무두셀라 효과는 시간이 지날수록 기억이 과장되거나 이상화되는 현상을 말합니다. 첫사랑은 강렬한 감정으로 각인되기 때문에 시간이 지날수록 더 아름답거나 아련하게 기억될 수 있습니다."
          		}
            }

        }
        ```
        """;
    }
}
