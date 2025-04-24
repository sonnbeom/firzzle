import React from 'react';

interface QuizAnswerProps {
  number: number;
}

const QuizAnswer = ({ number }: QuizAnswerProps) => {
  return (
    <div className='space-y-6 py-4 sm:py-6'>
      {/* 문제 번호 + 질문 */}
      <div className='flex items-start gap-4'>
        {/* 번호 */}
        <div className='text-[24px] font-bold text-gray-900 sm:text-[30px]'>
          {String(number).padStart(2, '0')}
        </div>

        {/* 질문 */}
        <p className='text-md font-medium text-gray-900 sm:text-lg'>
          머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한
          예측 성능이 떨어지는 현상을 과소적합(underfitting)이라고 한다.
        </p>
      </div>

      {/* 정답 여부 + 해설 */}
      <div className='rounded-xl border border-gray-50 bg-white px-6 py-4 shadow-sm transition-all sm:py-[16px]'>
        <div className='mb-2 text-center text-lg font-bold text-blue-400 sm:mb-[18px] sm:text-xl'>
          정답이에요!
        </div>
        <p className='text-md leading-relaxed text-gray-900 sm:text-lg'>
          머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한
          예측 성능이 떨어지는 현상을 과소적합(underfitting)이라고 합니다.
        </p>
      </div>
    </div>
  );
};

export default QuizAnswer;
