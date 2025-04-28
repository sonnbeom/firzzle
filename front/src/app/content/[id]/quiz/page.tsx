'use client';

import React, { useState, useEffect } from 'react';

// components
import QuizCard from '@/components/quiz/QuizCard';
// import QuizAnswer from '@/components/quiz/QuizAnswer';
import { Button } from '@/components/ui/button';

interface QuizContent {
  quizNo: number;
  question: string;
  answer: boolean;
  description: string;
}

const QuizPage = () => {
  const [selected, setSelected] = useState<Array<'O' | 'X' | null>>([null, null, null]);
  const [isCompleted, setIsCompleted] = useState(false);

  const quizContents: QuizContent[] = [
    {
      quizNo: 1,
      question:
        '머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한 예측 성능이 떨어지는 현상을 과소적합(underfitting)이라고 한다.',
      answer: false,
      description:
        '머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한 예측 성능이 떨어지는 현상을 과적합(overfitting)이라고 합니다.',
    },
    {
      quizNo: 2,
      question: '딥러닝에서 배치 정규화(Batch Normalization)는 학습 속도를 높이고 과적합을 줄이는데 도움을 준다.',
      answer: true,
      description: '배치 정규화는 각 레이어의 입력을 정규화하여 학습을 안정화시키고, 더 높은 학습률을 사용할 수 있게 하며, 규제(regularization) 효과도 있습니다.',
    },
    {
      quizNo: 3,
      question: 'K-평균 군집화(K-means clustering)는 지도 학습 알고리즘이다.',
      answer: false,
      description: 'K-평균 군집화는 레이블이 없는 데이터를 비슷한 그룹으로 묶는 비지도 학습 알고리즘입니다.',
    },
  ];

  useEffect(() => {
    if (selected.every((value) => value !== null)) {
      setIsCompleted(true);
    }
  }, [selected]);

  const handleSelect = (index: number, value: 'O' | 'X') => {
    const newSelected = [...selected];
    newSelected[index] = value;
    setSelected(newSelected);
  };

  return (
    <div className='relative min-h-screen w-full px-2 sm:px-10'>
      {/* QuizCard */}

      <div className='space-y-6 pb-28'>
        {quizContents.map((quiz, index) => (
          <QuizCard
            key={quiz.quizNo}
            quizNo={quiz.quizNo}
            question={quiz.question}
            selected={selected[index]}
            onSelect={(value) => handleSelect(index, value)}
          />
        ))}
      </div>
      <div className='bottom-0 left-0 w-full bg-white py-4'>
        <Button
          variant={isCompleted ? 'default' : 'disabled'}
          className={`w-full py-6 text-lg font-semibold ${isCompleted ? 'bg-blue-400 hover:bg-blue-400' : ''} text-white`}
        >
          도전하기
        </Button>
      </div>
    </div>
  );
};

export default QuizPage;
