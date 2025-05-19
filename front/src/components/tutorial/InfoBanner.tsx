import { memo } from 'react';

const InfoIcon = () => (
  <div
    className='mr-3 flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 font-bold text-blue-400'
    aria-hidden='true'
  >
    i
  </div>
);

const InfoBanner = () => {
  return (
    <aside className='container mx-auto my-8 px-4' role='complementary'>
      <div
        className='flex items-center rounded-lg border border-dashed border-blue-600 bg-blue-50 p-4 shadow-sm transition-colors hover:bg-blue-100'
        role='alert'
      >
        <InfoIcon />
        <div className='space-y-1'>
          <p className='font-medium text-blue-600'>
            학습 전 과정에서 러닝챗을 활용해보세요!
          </p>
          <p className='text-sm text-gray-600'>
            학습 중 언제든지 화면 좌측 채팅창을 활용하여 러닝챗을 사용할 수
            있습니다.
          </p>
        </div>
      </div>
    </aside>
  );
};

export default memo(InfoBanner);
