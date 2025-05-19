import { memo } from 'react';

const EditIcon = () => (
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
    aria-hidden='true'
  >
    <path d='M12 20h9' />
    <path d='M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z' />
  </svg>
);

const SNAP_COUNT = 4;

const StepFive = () => {
  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm'>
        <div className='mb-4 flex items-start'>
          <div className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'>
            5
          </div>

          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold'>
              스냅리뷰로 핵심 장면 정리
            </h3>
            <p className='mb-4 text-gray-600'>
              AI가 영상에서 추출한 핵심 장면을 &apos;인생네컷&apos; 스타일의
              프레임으로 정리하고, 각 장면에 대한 설명을 직접 작성할 수
              있습니다. 각 프레임을 클릭하면 해당 영상 구간이 재생되며, 작성한
              스냅리뷰는 링크로 공유하여 다른 사람들과 학습 내용을 나눌 수
              있습니다. 학습 내용을 자신만의 언어로 정리하면 기억에 오래
              남습니다.
            </p>

            <div className='rounded-lg bg-gray-50 p-6'>
              <div className='flex gap-4'>
                <div className='grid w-1/2 grid-cols-2 grid-rows-2 gap-4'>
                  {Array.from({ length: SNAP_COUNT }, (_, i) => i + 1).map(
                    (num) => (
                      <button
                        key={num}
                        className='aspect-video overflow-hidden rounded-lg border border-gray-200 bg-white transition-transform hover:scale-105'
                        aria-label={`스냅 ${num} 재생하기`}
                      >
                        <img
                          src={`/assets/images/snap${num}.png`}
                          alt={`스냅 ${num} 미리보기`}
                          className='h-full w-full object-cover'
                          loading='lazy'
                        />
                      </button>
                    ),
                  )}
                </div>

                <div className='flex w-1/2 flex-col'>
                  <textarea
                    placeholder='핵심 장면에 대한 설명을 입력하세요.'
                    className='h-full w-full cursor-default resize-none rounded-lg border border-gray-200 bg-gray-100 p-4 text-gray-500'
                    readOnly
                    aria-label='스냅 설명 입력창 (데모용)'
                  />

                  <div className='mt-2 self-end'>
                    <button
                      className='text-gray-500 transition-colors hover:text-gray-700'
                      aria-label='설명 편집하기'
                    >
                      <EditIcon />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default memo(StepFive);
