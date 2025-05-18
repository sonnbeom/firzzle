'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import HeaderButton from './HeaderButton';

const Header = () => {
  const pathname = usePathname();

  // share 페이지에서는 Header를 숨김
  if (pathname?.startsWith('/share/')) {
    return null;
  }

  return (
    <div className='sticky top-0 z-50 flex w-full items-center justify-between border-b border-gray-300 bg-white px-6 py-4 lg:py-5'>
      <Link
        href='/'
        className='relative h-[25px] w-[83px] md:h-[30px] md:w-[100px] lg:h-[35px] lg:w-[117px]'
      >
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vx'
          priority
          className='object-contain'
        />
      </Link>
      <HeaderButton />
    </div>
  );
};

export default Header;
