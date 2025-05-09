'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import OAuthButton from '../home/OAuthButton';

const HeaderButton = () => {
  const pathname = usePathname();

  return (
    <>
      {pathname == '/' ? (
        <OAuthButton
          url='https://kauth.kakao.com/oauth/authorize?'
          oauth='kakao'
          title='시작하기'
          className='hidden md:block'
        />
      ) : (
        <Link
          href='/mylearning/snapbook'
          className='bg-white font-medium text-gray-900 hover:bg-gray-50 md:text-lg lg:text-xl'
        >
          학습 내역
        </Link>
      )}
    </>
  );
};

export default HeaderButton;
