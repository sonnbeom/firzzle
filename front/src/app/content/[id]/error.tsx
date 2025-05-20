'use client';

import Link from 'next/link';

const Error = () => {
  return (
    <div className='flex h-full flex-col items-center justify-center gap-4'>
      <p className='text-2xl font-bold'>아직 생성되지 않은 페이지입니다.</p>
      <Link
        href='/'
        className='rounded bg-white px-4 py-3 text-lg text-blue-400'
      >
        메인페이지로 돌아가기
      </Link>
    </div>
  );
};

export default Error;
