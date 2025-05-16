'use client';

import React from 'react';
import InfoBanner from '@/components/tutorial/InfoBanner';
import StepFive from '@/components/tutorial/StepFive';
import StepFour from '@/components/tutorial/StepFour';
import StepOne from '@/components/tutorial/StepOne';
import StepSix from '@/components/tutorial/StepSix';
import StepThree from '@/components/tutorial/StepThree';
import StepTwo from '@/components/tutorial/StepTwo';

export default function FirzzlePage() {
  const steps = [
    { id: 1, title: 'URL 입력' },
    { id: 2, title: 'AI 요약노트' },
    { id: 3, title: '러닝챗' },
    { id: 4, title: 'OX 퀴즈' },
    { id: 5, title: '스냅 리뷰' },
    { id: 6, title: '관련 컨텐츠' },
  ];

  const handleScrollToStep = (stepId: number) => {
    const target = document.getElementById(`step${stepId}`);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className='min-h-screen bg-gray-50'>
      {/* 헤더 섹션 */}
      <div className='container mx-auto px-4 py-8'>
        <div className='py-12'>
          <h1 className='mb-6 flex items-center text-3xl font-bold'>
            <img
              src='/assets/images/Firzzle.png'
              alt='firzzle'
              className='mr-1 h-8'
            />
            <span className='text-blue-500'>
              {' '}
              과 함께 시작하는 스마트한 영상 학습
            </span>
          </h1>
          <p className='mb-4 text-gray-600'>
            firzzle은 온라인 동영상을 더 효과적으로 학습할 수 있도록 돕는 AI
            학습 코치입니다. <br />
            학습할 영상 링크를 입력하면 AI가 영상을 분석하여 영상 내용 요약본,
            퀴즈, 챗봇 기능까지 제공합니다.
          </p>
        </div>
      </div>

      {/* 프로세스 단계 섹션 */}
      <div className='w-full bg-[#F1F3FF]'>
        <div className='container mx-auto px-4 py-8'>
          <h2 className='mb-14 text-2xl font-bold text-[#343437]'>
            <span className='mr-2'>💡</span>
            Firzzle을 똑똑하게 사용하는 방법
          </h2>

          <div className='relative mb-8 flex items-center justify-between px-6 md:px-10'>
            {/* 연결선 */}
            <div className='absolute top-[25px] right-[5%] left-[5%] z-0 h-0.5 bg-gray-300'></div>

            {/* 단계 버튼 */}
            {steps.map((step) => (
              <div
                key={step.id}
                className='z-10 flex cursor-pointer flex-col items-center'
                onClick={() => handleScrollToStep(step.id)}
              >
                <div className='z-10 mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'>
                  {step.id}
                </div>
                <span className='text-center text-sm text-gray-600'>
                  {step.title}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 단계별 컴포넌트 (각각 id 추가 필요) */}
      <div id='step1'>
        <StepOne />
      </div>
      <div id='step2'>
        <StepTwo />
      </div>
      <div id='step3'>
        <StepThree />
      </div>

      <InfoBanner />

      <div id='step4'>
        <StepFour />
      </div>
      <div id='step5'>
        <StepFive />
      </div>
      <div id='step6'>
        <StepSix />
      </div>
    </div>
  );
}
