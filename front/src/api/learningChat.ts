import { externalApi } from './common/apiInstance';

// 학습모드 채팅 전송
export const postLearningChat = async () => {
  return await externalApi.post(`/chat`);
};

// 시험모드 채팅 전송
export const postExamChat = async (contentId: string) => {
  return await externalApi.get(`/contents/${contentId}/chat/exam/answer`);
};

// 학습모드 채팅 기록 조회
const getLearningChatHistory = async (contentId: string) => {
  return await externalApi.get(`/contents/${contentId}/chat/history`);
};

// 시험모드 채팅 기록 조회
const postExamChatHistory = async (contentId: string) => {
  return await externalApi.post(`/contents/${contentId}/chat/exam`);
};
