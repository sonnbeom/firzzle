import React from 'react';

// components
import QuizCard from '@/components/quiz/quizCard';
import { Button } from '@/components/ui/button';

const QuizPage = () => {
  return (
    <div>
      {/* QuizCard */}

      <div className='space-y-6 pb-28'>
        <QuizCard number={1} selected={null} />
        <QuizCard number={2} selected='O' />
        <QuizCard number={3} selected='X' />
      </div>
      <div className='fixed bottom-0 left-0 w-full bg-white py-4'>
        <Button
          variant='default'
          className='w-full bg-gray-200 py-6 text-lg font-semibold text-white hover:bg-gray-200 hover:text-white'
        >
          도전하기
        </Button>

        {/* QuizAnswer */}
        {/*
      <div className='space-y-6 pb-18'>
        <QuizAnswer number={1} />
        <QuizAnswer number={2} />
        <QuizAnswer number={3} />
        <div className='fixed bottom-0 left-0 w-full bg-white py-4'>
          <Button
            size='lg'
            variant='default'
            className='w-full bg-blue-400 py-6 text-lg font-semibold text-white hover:bg-blue-500 hover:text-white'
          >
            다음으로
          </Button>
        </div>  */}
      </div>
    </div>
  );
};

export default QuizPage;
