import BasicToaster from '@/components/common/BasicToaster';
import { useChatStore } from '@/stores/chatStore';
import { SendExamChatRequest } from '@/types/learningChat';

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
export const postExamChat = async (
  contentSeq: string,
  request: SendExamChatRequest,
) => {
  const response = await fetch(`/api/llm/${contentSeq}/exam`, {
    method: 'POST',
    body: JSON.stringify(request),
  });

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.data;
};

// 시험모드 채팅 기록 조회
export const getExamChatHistory = async (
  contentSeq: string,
  lastIndate?: string,
) => {
  const url = lastIndate
    ? `/api/llm/${contentSeq}/exam/history?lastIndate=${lastIndate}`
    : `/api/llm/${contentSeq}/exam/history`;

  const response = await fetch(url);

  const data = await response.json();

  if (response.status !== 200) {
    BasicToaster.error(data.message);
    return;
  }

  useChatStore.setState({
    currentExamSeq: data.info.currentExamSeq,
    solvedCount: data.info.solvedCount,
  });

  return data.data;
};

// 시험모드 새질문 생성
export const getNewExamChat = async (contentSeq: string) => {
  const response = await fetch(`/api/llm/${contentSeq}/exam`, {
    method: 'GET',
  });

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.data;
};
