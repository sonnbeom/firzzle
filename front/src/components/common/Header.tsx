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
    <div className='flex w-full items-center justify-between border-b border-gray-300 px-6 py-3 md:py-4 lg:py-6'>
      <div className='relative h-full w-[60px] md:w-[80px] lg:w-[100px]'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vx'
          priority
          className='object-contain'
        />
      </div>
      {!isLogin ? (
        <Button variant='default'>시작하기</Button>
      ) : (
        <Link
          href='/mylearning/snapbook'
          className='bg-white font-medium text-gray-900 hover:bg-gray-50 md:text-lg lg:text-xl'
        >
          학습 내역
        </Link>
      )}
    </div>
  );
};

export default Header;
