import { http, HttpResponse } from 'msw';
import { QuizData, QuizSubmitRequest, QuizSubmitResponse } from '@/types/quiz';

const quizData: QuizData[] = [
  {
    quizNo: '1',
    question:
      '머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한 예측 성능이 떨어지는 현상을 과소적합(underfitting)이라고 한다.',
    answer: false,
    description:
      '머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한 예측 성능이 떨어지는 현상을 과적합(overfitting)이라고 합니다.',
    timestamp: 100,
  },
  {
    quizNo: '2',
    question:
      '딥러닝에서 배치 정규화(Batch Normalization)는 학습 속도를 높이고 과적합을 줄이는데 도움을 준다.',
    answer: true,
    description:
      '배치 정규화는 각 레이어의 입력을 정규화하여 학습을 안정화시키고, 더 높은 학습률을 사용할 수 있게 하며, 규제(regularization) 효과도 있습니다.',
    timestamp: 100,
  },
  {
    quizNo: '3',
    question: 'K-평균 군집화(K-means clustering)는 지도 학습 알고리즘이다.',
    answer: false,
    description:
      'K-평균 군집화는 레이블이 없는 데이터를 비슷한 그룹으로 묶는 비지도 학습 알고리즘입니다.',
    timestamp: 100,
  },
];

export const quizHandlers = [
  // 퀴즈 조회 API
  http.get('/contents/:contentId/quiz', () => {
    const response = {
      data: quizData,
    };
    return HttpResponse.json(response);
  }),

  // 퀴즈 제출 API
  http.post('/contents/:contentId/quiz/submit', async ({ request }) => {
    const { answers } = (await request.json()) as QuizSubmitRequest;

    const results = answers.map((answer) => {
      const quiz = quizData.find((q) => q.quizNo === answer.quizNo);
      if (!quiz) throw new Error(`Quiz not found: ${answer.quizNo}`);

      return {
        quizNo: quiz.quizNo,
        question: quiz.question,
        correct: answer.answer === quiz.answer,
        answer: quiz.answer,
        description: quiz.description,
      };
    });

    const response: QuizSubmitResponse = {
      results,
      totalCount: answers.length,
      correctCount: results.filter((r) => r.correct).length,
    };

    return HttpResponse.json(response);
  }),
];
