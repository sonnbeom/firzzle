import React from 'react';

interface QuizAnswerProps {
  quizNo: number;
  question: string;
  answer: boolean;
  description: string;
}

const QuizAnswer = ({
  quizNo,
  question,
  answer,
  description,
}: QuizAnswerProps) => {
  return (
    <div className='space-y-6 py-4 sm:py-6'>
      {/* 문제 번호 + 질문 */}
      <div className='flex items-start gap-4'>
        {/* 번호 */}
        <div className='text-[24px] font-bold text-gray-900 sm:text-[30px]'>
          {String(quizNo).padStart(2, '0')}
        </div>

        {/* 질문 */}
        <p className='text-md font-medium text-gray-900 sm:text-lg'>
          {question}
        </p>
      </div>

      {/* 정답 여부 + 해설 */}
      <div className='rounded-xl border border-gray-50 bg-white px-6 py-4 shadow-sm transition-all sm:py-[16px]'>
        <div className={`mb-2 text-center text-lg font-bold sm:mb-[18px] sm:text-xl ${answer ? 'text-blue-400' : 'text-red-500'}`}>
          {answer ? '정답이에요!' : '틀렸어요!'}
        </div>
        <p className='text-md leading-relaxed text-gray-900 sm:text-lg'>
          {description}
        </p>
      </div>
    </div>
  );
};

export default QuizAnswer;
