import React from 'react';

// components
import { Button } from '@/components/ui/button';

interface QuizCardProps {
  selected: 'O' | 'X' | null;
  number: number;
}

const QuizCard = ({ selected, number }: QuizCardProps) => {
  return (
    <div className='rounded-[14px] border border-gray-50 bg-white px-8 py-2 shadow-sm transition-all sm:py-4'>
      {/* 문제 번호 */}
      <div className='text-center text-[24px] font-bold text-gray-900 sm:text-[30px]'>
        {String(number).padStart(2, '0')}
      </div>

      {/* 문제 */}
      <p className='text-md mb-6 text-left font-medium text-gray-900 sm:mb-10 sm:text-lg'>
        머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한
        예측 성능이 떨어지는 현상을 과소적합(underfitting)이라고 한다.
      </p>

      {/* 버튼들 */}
      <div className='mb-4 flex flex-row gap-2'>
        {/* O 버튼*/}
        <div className='w-full'>
          <Button
            size='lg'
            variant={selected === 'O' ? 'default' : 'outline'}
            className={`relative flex w-full items-center justify-center py-6 text-lg font-semibold sm:text-xl ${
              selected === 'O'
                ? 'bg-blue-400 text-white hover:bg-blue-400'
                : 'border-blue-400 text-blue-400 hover:bg-blue-50 hover:text-blue-400'
            }`}
          >
            <span className='absolute left-4 text-2xl sm:text-[36px]'>O</span>
            <span className='text-center'>맞아요</span>
          </Button>
        </div>

        {/* X 버튼*/}
        <div className='w-full'>
          <Button
            size='lg'
            variant={selected === 'X' ? 'default' : 'outline'}
            className={`relative flex w-full items-center justify-center py-6 text-lg font-semibold sm:text-xl ${
              selected === 'X'
                ? 'bg-red-500 text-white hover:bg-red-500'
                : 'border-red-500 text-red-500 hover:bg-red-50 hover:text-red-500'
            }`}
          >
            <span className='absolute left-4 text-2xl sm:text-[36px]'>X</span>
            <span className='text-center'>아니에요</span>
          </Button>
        </div>
      </div>
    </div>
  );
};

export default QuizCard;
