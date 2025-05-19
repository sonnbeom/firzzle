import { useState } from 'react';

const StepTwo = () => {
  const [mode, setMode] = useState<'easy' | 'expert'>('easy');

  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm'>
        <div className='mb-4 flex items-start'>
          <div className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'>
            2
          </div>
          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold'>AI 요약노트</h3>
            <p className='mb-2 text-gray-600'>
              AI가 영상을 분석하여 주제별로 요약한 내용을 확인할 수 있습니다.
              난이도에 따라{' '}
              <span className='font-bold text-gray-950'>쉽게 설명해주세요</span>{' '}
              또는{' '}
              <span className='font-bold text-gray-950'>배경지식이 있어요</span>{' '}
              모드를 선택할 수 있으며, 각 요약 옆 타임스탬프를 클릭하면 해당
              영상 구간으로 바로 이동합니다.
            </p>

            <div className='mb-4 flex w-full gap-2'>
              <button
                onClick={() => setMode('easy')}
                className={`flex-1 cursor-pointer rounded-md px-6 py-3 font-medium ${
                  mode === 'easy'
                    ? 'bg-blue-400 text-white'
                    : 'bg-gray-100 text-gray-700'
                }`}
              >
                쉽게 설명해주세요
              </button>
              <button
                onClick={() => setMode('expert')}
                className={`flex-1 cursor-pointer rounded-md px-6 py-3 font-medium ${
                  mode === 'expert'
                    ? 'bg-blue-400 text-white'
                    : 'bg-gray-100 text-gray-700'
                }`}
              >
                배경지식이 있어요
              </button>
            </div>

            <div className='rounded-lg bg-gray-50 p-6'>
              <div className='mb-4'>
                <h4 className='mb-1 font-bold text-gray-950'>
                  1. 인공지능의 기초 개념{' '}
                  <span className='ml-2 text-sm font-normal text-gray-500'>
                    03:24
                  </span>
                </h4>
                {mode === 'easy' ? (
                  <p className='text-gray-700'>
                    인공지능(AI)은 인간의 학습능력과 추론능력, 지각능력,
                    자연언어의 이해능력 등을 컴퓨터 프로그램으로 실현한
                    기술입니다. 머신러닝은 AI의 한 분야로, 데이터를 통해 스스로
                    학습하는 알고리즘을 개발하는 방법론입니다. 예를 들어, 많은
                    사진을 학습한 알고리즘이 고양이의 개념을 구분하거나, 과거의
                    거래 데이터를 학습해 주가를 예측하는 것처럼, 경험(데이터)를
                    통해 알고리즘이 스스로 성능을 개선해 나가는 것이 머신러닝의
                    핵심입니다.
                  </p>
                ) : (
                  <ul className='list-disc pl-5 text-gray-700'>
                    <li>
                      AI: 인간의 학습·추론·지각·언어 이해 능력을 구현한 기술
                    </li>
                    <li>
                      머신러닝: AI의 하위 분야, 데이터 기반 학습 알고리즘 개발
                    </li>
                    <li>예시: 이미지 인식을 통해 고양이 구분 / 주가 예측</li>
                    <li>핵심: 경험(데이터) 기반의 성능 개선</li>
                  </ul>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StepTwo;
