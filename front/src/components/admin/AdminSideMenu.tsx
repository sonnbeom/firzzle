'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from '../ui/button';

const AdminSideMenu = () => {
  const pathname = usePathname();

  return (
    <div className='items-col flex h-full w-full flex-col gap-3 bg-blue-50 p-3'>
      <div className='relative h-[80px] w-[115px]'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vw,'
          className='object-contain'
        />
      </div>
      <Link href='/admin' className='w-full'>
        <Button
          variant={pathname === '/admin' ? 'default' : 'text'}
          className={'w-full justify-start ' + (pathname === '/admin' ? 'bg-white text-left text-blue-400' : 'bg-transparent')}
        >
          인기 컨텐츠
        </Button>
      </Link>
      <Link href='/admin/learninginsights' className='w-full'>
        <Button
          variant={pathname === '/admin/learninginsights' ? 'default' : 'text'}
          className={'w-full justify-start ' + (pathname === '/admin/learninginsights' ? 'bg-white text-left text-blue-400' : 'bg-transparent')}
        >
          학습 기능 분석
        </Button>
      </Link>
      <Link href='/admin/strategyboard' className='w-full'>
        <Button
          variant={pathname === '/admin/strategyboard' ? 'default' : 'text'}
          className={'w-full justify-start ' + (pathname === '/admin/strategyboard' ? 'bg-white text-left text-blue-400' : 'bg-transparent')}
        >
          기획/전략
        </Button>
      </Link>
      <hr className='w-full border-gray-200' />
    </div>
  );
};

export default AdminSideMenu;
