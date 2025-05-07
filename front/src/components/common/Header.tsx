'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';
import { Button } from '../ui/button';

const Header = () => {
  const pathname = usePathname();
  const [isLogin, setIsLogin] = useState(true);
  const isAdminPage = pathname?.includes('/admin');

  if (pathname?.startsWith('/share')) {
    return null;
  }

  return isAdminPage ? (
    <></>
  ) : (
    <div className='flex w-full items-center justify-between border-b border-gray-300 px-8'>
      <div className='relative h-[80px] w-[115px]'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='110vx'
          priority
          className='object-contain'
        />
      </div>
      {!isLogin ? (
        <Button variant='default'>시작하기</Button>
      ) : (
        <Link
          href='/mylearning/snapbook'
          className='bg-white text-xl font-medium text-gray-950 hover:bg-gray-50'
        >
          학습 내역
        </Link>
      )}
    </div>
  );
};

export default Header;
