// components/StepSix.tsx
import React from 'react';

const StepSix: React.FC = () => {
  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm'>
        <div className='mb-4 flex items-start'>
          <div className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'>
            6
          </div>
          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold'>관련 컨텐츠 추천</h3>
            <p className='mb-4 text-gray-600'>
              학습한 내용과 관련된 강의와 전문가가 자동으로 추천됩니다. 추천
              강의를 클릭하면 바로 해당 강의로 이동할 수 있으며, 검은색 페이지를
              통해 더 깊이 있는 학습이 가능합니다. 관련 주제로 학습을 이어가며
              지식의 폭을 넓혀보세요.
            </p>

            <div className='mb-8'>
              <div className='mb-4 flex items-center justify-between'>
                <h4 className='font-medium text-blue-600'>
                  인공지능에 관련된 강의를 추천해드릴게요
                </h4>
                <div className='flex items-center'>
                  <div className='mx-1 h-2 w-2 rounded-full bg-gray-300'></div>
                  <div className='mx-1 h-2 w-2 rounded-full bg-gray-500'></div>
                  <div className='mx-1 h-2 w-2 rounded-full bg-gray-300'></div>
                </div>
              </div>

              <div className='flex space-x-4'>
                {[1, 2, 3].map((item) => (
                  <div
                    key={item}
                    className='overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm'
                    style={{ width: '180px' }}
                  >
                    <div className='flex items-center justify-center bg-blue-500 p-3 text-white'>
                      <div className='text-center'>
                        <div className='text-sm'>Game Programmer</div>
                        <div className='text-xs'>All in One</div>
                      </div>
                    </div>
                    <div className='truncate p-2 text-xs text-gray-500'>
                      [게임 프로그래머 입문 훈련과정] C++ & 자료구조/알고리즘
                      &STL & 게임 수학...
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <div className='mb-4 flex items-center justify-between'>
                <h4 className='font-medium text-blue-600'>
                  인공지능 전문가와 대화해보세요
                </h4>
                <div className='flex items-center'>
                  <div className='mx-1 h-2 w-2 rounded-full bg-gray-300'></div>
                  <div className='mx-1 h-2 w-2 rounded-full bg-gray-500'></div>
                  <div className='mx-1 h-2 w-2 rounded-full bg-gray-300'></div>
                </div>
              </div>

              <div className='flex space-x-4'>
                {[1, 2, 3].map((item) => (
                  <div
                    key={item}
                    className='overflow-hidden rounded-lg border border-gray-200 bg-white p-3 text-center shadow-sm'
                    style={{ width: '180px' }}
                  >
                    <div className='mx-auto mb-2'>
                      <div className='mx-auto h-16 w-16 overflow-hidden rounded-full bg-gray-200'>
                        <img
                          src='/assets/images/expert.jpg'
                          alt='전문가'
                          className='h-full w-full object-cover'
                        />
                      </div>
                    </div>
                    <div className='mb-1 font-medium'>박정수</div>
                    <div className='mb-1 text-xs text-gray-500'>
                      AI 연구교수
                    </div>
                    <div className='text-xs text-gray-500'>
                      ML Planning & Analytics, Deep Learning Leader
                    </div>
                    <div className='mt-2 flex items-center justify-center'>
                      <div className='mr-1 rounded-full bg-gray-100 px-2 py-1 text-xs'>
                        👍 14
                      </div>
                      <div className='rounded-full bg-gray-100 px-2 py-1 text-xs'>
                        💬 5
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StepSix;
