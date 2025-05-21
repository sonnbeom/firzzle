export type Mode = '학습모드' | '시험모드';

export interface LearningChat {
  chatSeq: number;
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

// 시험모드 채팅 전송 요청
export type SendExamChatRequest = {
  exam_seq: string;
  answer: string;
};

// 시험모드 채팅 전송 응답
export type SendExamChatResponse = {
  explanation: string;
  indate: string;
};

// 시험모드 채팅 목록 조회 응답
export type GetExamChatHistoryResponse = {
  info: {
    currentExamSeq: string;
    solved_count: number;
  };
  historyList: LearningChat[];
};

// 시험모드 새질문 응답
export type NewExamChatResponse = {
  exam_seq: string;
  question: string;
};
