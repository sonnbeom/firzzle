import { QuizData, QuizSubmitRequest, QuizSubmitResponse } from '@/types/quiz';
import { api } from './common/apiInstance';

// 퀴즈 조회
export const getQuiz = async (contentSeq: string) => {
  const { data } = await api.get<{ content: QuizData }>(
    `/learning/contents/${contentSeq}/quiz`,
  );
  return data.content;
};

// 퀴즈 정답 제출
export const submitQuizAnswers = async (
  contentSeq: string,
  request: QuizSubmitRequest,
) => {
  const { data } = await api.post<QuizSubmitResponse, QuizSubmitRequest>(
    `/learning/contents/${contentSeq}/quiz`,
    { body: request },
  );
  return data;
};
