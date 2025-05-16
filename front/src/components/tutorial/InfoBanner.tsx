// components/InfoBanner.tsx
import React from 'react';

const InfoBanner: React.FC = () => {
  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='flex items-center rounded-lg border border-dashed border-[#4f46e5] bg-blue-50 p-4'>
        <div className='mr-3 flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 font-bold text-blue-400'>
          i
        </div>
        <div>
          <p className='font-medium text-blue-400'>
            학습 전 과정에서 러닝챗을 활용해보세요!
          </p>
          <p className='text-sm text-gray-600'>
            학습 중 언제든지 화면 좌측 채팅창을 활용하여 러닝챗을 사용할 수
            있습니다.
          </p>
        </div>
      </div>
    </div>
  );
};

export default InfoBanner;
