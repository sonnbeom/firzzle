import React from 'react';

// components
import QuizCard from '@/components/quiz/QuizCard';
// import QuizAnswer from '@/components/quiz/QuizAnswer';
import { Button } from '@/components/ui/button';

const QuizPage = () => {
  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      {/* QuizCard */}

      <div className='space-y-6 pb-28'>
        <QuizCard number={1} selected={null} />
        <QuizCard number={2} selected='O' />
        <QuizCard number={3} selected='X' />
      </div>
      <div className='bottom-0 left-0 w-full bg-white py-4'>
        <Button
          variant='disabled'
          className='w-full py-6 text-lg font-semibold text-white'
        >
          도전하기
        </Button>

        {/* QuizAnswer */}
        {/* <div className='relative min-h-screen w-full'>
        <div className='space-y-6 pb-36'>
          <QuizAnswer number={1} />
          <QuizAnswer number={2} />
          <QuizAnswer number={3} />
        </div>

        <div className='absolute bottom-0 left-0 w-full bg-white py-4'>
          <Button
            size='lg'
            variant='default'
            className='w-full bg-blue-400 py-6 text-lg font-semibold text-white hover:bg-blue-400 hover:text-white pointer-cursor'
          >
            다음으로
          </Button>
        </div>  */}
      </div>
    </div>
  );
};

export default QuizPage;
