'use client';

import Image from 'next/image';
import NavButton from './NavButton';

const AdminSideMenu = () => {
  return (
    <div className='flex w-full flex-row items-center gap-2 bg-blue-50 p-2 lg:h-full lg:flex-col lg:items-start lg:gap-3 lg:p-3'>
      <div className='relative hidden h-[80px] w-[115px] lg:block'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vw,'
          className='object-contain'
        />
      </div>
      <NavButton href='/admin'>인기 컨텐츠</NavButton>
      <NavButton href='/admin/learninginsights'>학습 기능 분석</NavButton>
      <NavButton href='/admin/strategyboard'>기획/전략</NavButton>
      <hr className='hidden w-full border-gray-200 lg:block' />
    </div>
  );
};

export default AdminSideMenu;
