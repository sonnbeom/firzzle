export type Mode = '학습모드' | '시험모드';

export interface LearningChat {
  chatSeq: string;
  chatText: string;
  indate: string;
  type: '0' | '1'; // 0은 사용자, 1은 챗봇
}

// 학습모드 채팅 전송 요청
export type SendLearningChatRequest = {
  question: string;
};

// 학습모드 채팅 전송 응답
export type SendLearningChatResponse = {
  answer: string;
  indate: string;
};

// 학습모드 채팅 목록 조회 응답
export type GetLearningChatHistoryResponse = LearningChat[];
