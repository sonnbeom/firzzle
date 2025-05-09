'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

const AuthCallback = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const accessToken = searchParams.get('accessToken');

        await fetch(`/auth/callback?accessToken=${accessToken}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });
      } catch (error) {
        console.error('OAuth 인증 콜백 실패', error);
        router.push('/');
      }
    };

    handleCallback();
  }, [router, searchParams]);

  return null;
};

export default AuthCallback;
