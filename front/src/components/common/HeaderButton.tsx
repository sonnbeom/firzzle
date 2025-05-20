'use client';

import Link from 'next/link';
import { useState } from 'react';
import { useEffect } from 'react';
import { getCookie } from '@/actions/auth';
import { logout } from '@/api/auth';
import OAuthButton from '../home/OAuthButton';

interface HeaderButtonProps {
  accessToken: string | null;
}

const HeaderButton = ({ accessToken }: HeaderButtonProps) => {
  const [token, setToken] = useState(accessToken);

  useEffect(() => {
    const checkAccessToken = async () => {
      const newToken = await getCookie('accessToken');
      setToken(newToken);
    };

    checkAccessToken();
  }, [token]);

  return (
    <>
      {!token ? (
        <OAuthButton
          url='https://kauth.kakao.com/oauth/authorize'
          oauth='kakao'
          title='시작하기'
          className='hidden py-1 md:block'
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
