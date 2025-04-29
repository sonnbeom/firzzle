import React from 'react';
import QuizContainer from '@/components/quiz/QuizContainer';

interface QuizContent {
  quizNo: string;
  question: string;
  answer: boolean;
  description: string;
  timestamp: number;
}

// api 연결 이후 서버의 데이터 페칭 로직으로 대체
const quizContents: QuizContent[] = [
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

function QuizPage() {
  return <QuizContainer quizContents={quizContents} />;
}

export default QuizPage;
