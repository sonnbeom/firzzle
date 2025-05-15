export type Mode = '학습모드' | '시험모드';

// 학습모드 채팅 전송 요청
export type SendLearningChatRequest = {
  question: string;
};

// 학습모드 채팅 전송 응답
export type SendLearningChatResponse = {
  answer: string;
  referenceTime: number;
};
