import { ApiResponseWithData } from '@/types/common/apiResponse';
import { QuizData, QuizSubmitRequest, QuizSubmitResponse } from '@/types/quiz';
import { externalApi } from './common/apiInstance';

// 퀴즈 조회
export const getQuiz = (contentId: string) => {
  return externalApi.get(`/contents/${contentId}/quiz`) as Promise<
    ApiResponseWithData<QuizData[]>
  >;
};

// 퀴즈 정답 제출
export const submitQuizAnswers = (
  contentId: string,
  request: QuizSubmitRequest,
) => {
  return externalApi.post<QuizSubmitResponse, QuizSubmitRequest>(
    `/contents/${contentId}/quiz/submit`,
    {
      body: request,
    },
  );
};
