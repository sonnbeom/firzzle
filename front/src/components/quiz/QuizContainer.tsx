'use client';

import React, { useState, useCallback, useMemo } from 'react';
import { Button } from '@/components/ui/button';
import QuizAnswer from './QuizAnswer';
import QuizCard from './QuizCard';

interface QuizContent {
  quizNo: number;
  question: string;
  answer: boolean;
  description: string;
}

interface QuizContainerProps {
  quizContents: QuizContent[];
}

const QuizContainer = ({ quizContents }: QuizContainerProps) => {
  // 1 ~ 3개 문제 갯수에 맞게 동적 배열
  const [selected, setSelected] = useState<Array<'O' | 'X' | null>>(() =>
    new Array(quizContents.length).fill(null),
  );
  const [showAnswer, setShowAnswer] = useState(false);

  const handleSelect = useCallback((index: number, value: 'O' | 'X') => {
    setSelected((prev) => {
      const newSelected = [...prev];
      newSelected[index] = value;
      return newSelected;
    });
  }, []);

  const isCompleted = useMemo(
    () => selected.every((value) => value !== null),
    [selected],
  );

  return (
    <div className='relative min-h-screen w-full px-2 sm:px-10'>
      <div className='space-y-6 pb-28'>
        {/* 퀴즈 컴포넌트 */}
        {quizContents.map((quiz, index) =>
          showAnswer ? (
            <QuizAnswer
              key={quiz.quizNo}
              quizNo={quiz.quizNo}
              question={quiz.question}
              answer={selected[index] === 'O' ? quiz.answer : !quiz.answer}
              description={quiz.description}
            />
          ) : (
            <QuizCard
              key={quiz.quizNo}
              quizNo={quiz.quizNo}
              question={quiz.question}
              selected={selected[index]}
              onSelect={(value) => handleSelect(index, value)}
            />
          ),
        )}
      </div>
      {/* 버튼 */}
      {!showAnswer && (
        <div className='bottom-0 left-0 w-full bg-white py-4'>
          <Button
            variant={isCompleted ? 'default' : 'disabled'}
            className={`w-full py-6 text-lg font-semibold ${isCompleted ? 'bg-blue-400 hover:bg-blue-400' : ''} text-white`}
            onClick={() => isCompleted && setShowAnswer(true)}
          >
            도전하기
          </Button>
        </div>
      )}
    </div>
  );
};

export default QuizContainer;
