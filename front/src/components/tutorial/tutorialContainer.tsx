'use client';

import { useMemo } from 'react';
import InfoBanner from '@/components/tutorial/InfoBanner';
import StepFive from '@/components/tutorial/StepFive';
import StepFour from '@/components/tutorial/StepFour';
import StepOne from '@/components/tutorial/StepOne';
import StepSix from '@/components/tutorial/StepSix';
import StepThree from '@/components/tutorial/StepThree';
import StepTwo from '@/components/tutorial/StepTwo';

const TUTORIAL_STEPS = [
  { id: 1, title: 'URL 입력', Component: StepOne },
  { id: 2, title: 'AI 요약노트', Component: StepTwo },
  { id: 3, title: '러닝챗', Component: StepThree },
  { id: 4, title: 'OX 퀴즈', Component: StepFour },
  { id: 5, title: '스냅 리뷰', Component: StepFive },
  { id: 6, title: '관련 컨텐츠', Component: StepSix },
] as const;

const TutorialContainer = () => {
  const handleScrollToStep = (stepId: number) => {
    const target = document.getElementById(`step${stepId}`);
    target?.scrollIntoView({ behavior: 'smooth' });
  };

  const renderSteps = useMemo(
    () =>
      TUTORIAL_STEPS.map((step) => (
        <button
          key={step.id}
          onClick={() => handleScrollToStep(step.id)}
          className='rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50'
        >
          {step.title}
        </button>
      )),
    [],
  );

  const renderContent = useMemo(
    () =>
      TUTORIAL_STEPS.map(({ id, Component }) => (
        <div key={id} id={`step${id}`}>
          <Component />
        </div>
      )),
    [],
  );

  return (
    <div className='min-h-screen bg-gray-50'>
      <div className='container mx-auto px-4 py-8'>
        <h1 className='mb-8 text-center text-3xl font-bold'>
          Firzzle 서비스 이용 가이드
        </h1>

        <div className='mb-8 flex justify-center space-x-4'>{renderSteps}</div>

        <InfoBanner />

        <div className='space-y-8'>{renderContent}</div>
      </div>
    </div>
  );
};

export default TutorialContainer;
