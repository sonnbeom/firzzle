'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from '../ui/button';

const AdminSideMenu = () => {
  const pathname = usePathname();

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
      <Link href='/admin' className='flex-1 lg:w-full lg:flex-none'>
        <Button
          variant={pathname === '/admin' ? 'default' : 'text'}
          className={
            'h-full w-full justify-center px-2 text-sm lg:justify-start lg:px-4 lg:text-base ' +
            (pathname === '/admin'
              ? 'bg-white text-blue-400'
              : 'bg-transparent')
          }
        >
          인기 컨텐츠
        </Button>
      </Link>
      <Link
        href='/admin/learninginsights'
        className='flex-1 lg:w-full lg:flex-none'
      >
        <Button
          variant={pathname === '/admin/learninginsights' ? 'default' : 'text'}
          className={
            'h-full w-full justify-center px-2 text-sm lg:justify-start lg:px-4 lg:text-base ' +
            (pathname === '/admin/learninginsights'
              ? 'bg-white text-blue-400'
              : 'bg-transparent')
          }
        >
          학습 기능 분석
        </Button>
      </Link>
      <Link
        href='/admin/strategyboard'
        className='flex-1 lg:w-full lg:flex-none'
      >
        <Button
          variant={pathname === '/admin/strategyboard' ? 'default' : 'text'}
          className={
            'h-full w-full justify-center px-2 text-sm lg:justify-start lg:px-4 lg:text-base ' +
            (pathname === '/admin/strategyboard'
              ? 'bg-white text-blue-400'
              : 'bg-transparent')
          }
        >
          기획/전략
        </Button>
      </Link>
      <hr className='hidden w-full border-gray-200 lg:block' />
    </div>
  );
};

export default AdminSideMenu;
