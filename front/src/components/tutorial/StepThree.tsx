// components/StepThree.tsx
import React from 'react';

const StepThree: React.FC = () => {
  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm'>
        <div className='mb-4 flex items-start'>
          <div className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'>
            3
          </div>
          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold'>러닝챗으로 심화 학습</h3>
            <p className='mb-4 text-gray-600'>
              러닝챗을 통해 학습 내용에 대해 질문할 수 있고, 자신의 이해도를
              테스트할 수 있습니다. 학습모드에서는 자유롭게 질문하고 답변을 받을
              수 있으며, 시험모드에서는 AI가 출제하는 문제를 풀고 피드백을 받을
              수 있습니다. 학습 중 언제든지 대화를 통해 학습을 심화하세요.
            </p>

            <div className='mb-4 flex'>
              <button className='mr-2 rounded-md border border-gray-300 bg-white px-6 py-3 font-medium text-gray-600'>
                학습모드
              </button>
              <button className='rounded-md bg-blue-400 px-6 py-3 font-medium text-white'>
                시험모드
              </button>
            </div>

            <div className='rounded-lg bg-gray-50 p-6'>
              <div className='mb-4'>
                <div className='mb-4 inline-block rounded-lg bg-blue-300 px-6 py-3 text-white'>
                  머신러닝과 딥러닝은 어떻게 다른가요?
                </div>
                <p className='mb-4 text-gray-700'>
                  인공지능(AI)은 인간의 지능을 모방하는 넓은 개념이고,
                  머신러닝은 AI의 한 분야로 데이터를 기반으로 스스로 학습하는
                  알고리즘을 개발하는 방법론입니다.
                </p>

                <div className='mt-4 flex items-center justify-between rounded-md border border-gray-200 bg-white p-2'>
                  <p className='ml-2 text-sm text-gray-500'>
                    어떤 정보가 궁금하신가요?
                  </p>
                  <button className='text-[#4f46e5]'>
                    <svg
                      xmlns='http://www.w3.org/2000/svg'
                      width='20'
                      height='20'
                      viewBox='0 0 24 24'
                      fill='none'
                      stroke='currentColor'
                      strokeWidth='2'
                      strokeLinecap='round'
                      strokeLinejoin='round'
                    >
                      <path d='M12 20h9'></path>
                      <path d='M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z'></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StepThree;
