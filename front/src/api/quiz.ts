import { api } from './common/apiInstance';
import { QuizData, QuizSubmitRequest, QuizSubmitResponse } from '@/types/quiz';

// 퀴즈 조회
export const getQuiz = (contentId: string) => {
  return api.get<QuizData[]>(`/contents/${contentId}/quiz`);
};

// 퀴즈 정답 제출
export const submitQuizAnswers = (contentId: string, request: QuizSubmitRequest) => {
  return api.post<QuizSubmitResponse, QuizSubmitRequest>(`/contents/${contentId}/quiz/submit`, { body: request });
};
