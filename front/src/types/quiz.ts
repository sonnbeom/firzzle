// 퀴즈 옵션 타입
export interface QuizOption {
  optionSeq: number; // 보기번호
  text: string; // 보기내용
}

// 퀴즈 데이터 타입
export interface QuizData {
  contentSeq: number; // 영상번호
  questions: {
    questionSeq: number; // 질문번호
    text: string; // 질문내용
    type: string; // 질문유형
    timestamp: number; // 영상시간
    formattedTimestamp: string; // 영상시간 변환
    options: QuizOption[];
    userAnswer: {
      selectedOptionSeq: number; // 선택 답변
      isCorrect: boolean; // 정답 유무
    } | null;
  }[];
  submission: {
    submissionSeq: number;
    correctAnswers: number; // 맞은 문제
    totalQuestions: number; // 전체 문제
    scorePercentage: number; // 점수
    indate: string; // 제출 시간
  } | null;
}

// 퀴즈 제출 요청 타입
export interface QuizSubmitRequest {
  answers: {
    questionSeq: number;
    selectedAnswer: string;
  }[];
}

// 퀴즈 제출 응답 타입
export interface QuizSubmitResponse {
  submissionSeq: number;
  correctAnswers: number;
  totalQuestions: number;
  scorePercentage: number;
  indate: string;
}

// 퀴즈 카드 Props 타입
export interface QuizCardProps {
  selected: number | null;
  questionSeq: number;
  text: string;
  options: QuizOption[];
  onSelect: (optionSeq: number) => void;
}

// 퀴즈 답안 Props 타입
export interface QuizAnswerProps {
  questionSeq: number;
  text: string;
  correct: boolean;
  description: string;
  timestamp: number;
  selectedOption?: QuizOption;
  correctOption?: QuizOption;
}
