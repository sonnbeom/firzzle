'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { logout } from '@/api/auth';
import BasicToaster from '../common/BasicToaster';
import OAuthButton from '../home/OAuthButton';
const HeaderButton = () => {
  const pathname = usePathname();

  const handleLogout = async () => {
    try {
      const response = await logout();
      BasicToaster.default(response, { id: 'logout' });
    } catch (error) {
      BasicToaster.error(error.message, { id: 'logout', duration: 2000 });
    }
  };

  return (
    <>
      {pathname == '/' ? (
        <OAuthButton
          url='https://kauth.kakao.com/oauth/authorize'
          oauth='kakao'
          title='시작하기'
          className='hidden md:block'
        />
      ) : (
        <div className='flex items-center gap-8'>
          <Link
            href='/mylearning/snapbook'
            className='bg-white font-medium text-gray-900 hover:bg-gray-50 md:text-lg lg:text-xl'
          >
            학습내역
          </Link>
          <button
            className='bg-white font-medium text-gray-900 hover:bg-gray-50 md:text-lg lg:text-xl'
            onClick={handleLogout}
          >
            로그아웃
          </button>
        </div>
      )}
    </>
  );
};

export default HeaderButton;
