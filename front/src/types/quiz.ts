// 퀴즈 데이터 타입
export interface QuizData {
  quizNo: string;
  question: string;
  answer: boolean;
  description: string;
  timestamp: number;
}

// 퀴즈 제출 요청 타입
export interface QuizSubmitRequest {
  answers: { quizNo: string; answer: boolean }[];
}

// 퀴즈 제출 응답 타입
export interface QuizSubmitResponse {
  results: {
    quizNo: string;
    question: string;
    correct: boolean; // 정답여부
    answer: boolean; // 정답
    description: string;
    timestamp?: number;
  }[];
  totalCount: number;
  correctCount: number;
}
