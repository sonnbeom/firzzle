'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { getCookie } from '@/actions/auth';
import { logout } from '@/api/auth';
import OAuthButton from '../home/OAuthButton';
const HeaderButton = () => {
  const [accessToken, setAccessToken] = useState(null);

  useEffect(() => {
    const getAccessToken = async () => {
      const token = await getCookie('accessToken');
      setAccessToken(token);
    };
    getAccessToken();
  }, [accessToken]);

  return (
    <>
      {!accessToken ? (
        <OAuthButton
          url='https://kauth.kakao.com/oauth/authorize'
          oauth='kakao'
          title='시작하기'
          className='hidden md:block'
        />
      ) : (
        <div className='flex items-center gap-4 lg:gap-8'>
          <Link
            href='/mylearning/contents'
            className='bg-white font-medium text-gray-900 hover:bg-gray-50 lg:text-lg'
          >
            학습내역
          </Link>
          <button
            className='bg-white font-medium text-gray-900 hover:bg-gray-50 lg:text-lg'
            onClick={logout}
          >
            로그아웃
          </button>
        </div>
      )}
    </>
  );
};

export default HeaderButton;
