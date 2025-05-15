import { QuizSubmitRequest, QuizData } from '@/types/quiz';

// 퀴즈 조회
export const getQuiz = async (
  contentSeq: string,
): Promise<{ data: QuizData }> => {
  const response = await fetch(`/api/learning/contents/${contentSeq}/quiz`);

  const data = await response.json();
  return data;
};

// 퀴즈 정답 제출
export const submitQuizAnswers = async (
  contentSeq: string,
  request: QuizSubmitRequest,
) => {
  const response = await fetch(`/api/learning/contents/${contentSeq}/quiz`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};
