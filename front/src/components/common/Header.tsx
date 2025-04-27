'use client';

import Image from 'next/image';
import { usePathname } from 'next/navigation';
import { useState } from 'react';
import { Button } from '../ui/button';

const Header = () => {
  const [isLogin, setIsLogin] = useState(true);
  const pathname = usePathname();
  const isAdminPage = pathname?.includes('/admin');

  return isAdminPage ? (
    <></>
  ) : (
    <div className='flex w-full items-center justify-between border-b border-gray-300 px-8'>
      <div className='relative h-[80px] w-[115px]'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vw,'
          className='object-contain'
        />
      </div>
      {!isLogin ? (
        <Button variant='default'>시작하기</Button>
      ) : (
        <Button variant='text'>학습 내역</Button>
      )}
    </div>
  );
};

export default Header;
