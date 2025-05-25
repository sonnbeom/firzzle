'use client';

import Link from 'next/link';

const Error = () => {
  return (
    <div className='flex h-full flex-col items-center justify-center gap-4'>
      <p className='text-2xl font-bold'>
        컨텐츠를 분석 중입니다. 나중에 확인해주세요.
      </p>
      <Link
        href='/mylearning/contents'
        className='rounded bg-white px-4 py-3 text-lg text-blue-400'
      >
        학습 내역 확인하기
      </Link>
    </div>
  );
};

export default Error;
