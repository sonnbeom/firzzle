import BasicToaster from '@/components/common/BasicToaster';
import { api } from './common/apiInstance';

// 학습모드 채팅 전송
export const postLearningChat = async (
  contentSeq: string,
  question: string,
) => {
  const response = await fetch(`/api/llm/${contentSeq}/chat`, {
    method: 'POST',
    body: JSON.stringify({ question }),
  });

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.data;
};

// 학습모드 채팅 기록 조회
export const getLearningChatHistory = async (
  contentSeq: string,
  lastIndate?: string,
) => {
  const url = lastIndate
    ? `/api/llm/${contentSeq}/chat?lastIndate=${lastIndate}`
    : `/api/llm/${contentSeq}/chat`;

  const response = await fetch(url, {
    method: 'GET',
  });

  const data = await response.json();

  if (response.status !== 200) {
    BasicToaster.error(data.message);
    return;
  }

  return data.data;
};

// 시험모드 채팅 전송
export const postExamChat = async (contentId: string) => {
  return await api.get(`/contents/${contentId}/chat/exam/answer`);
};

// 시험모드 채팅 기록 조회
const postExamChatHistory = async (contentId: string) => {
  return await api.post(`/contents/${contentId}/chat/exam`);
};
