'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';
import { internalApi } from '@/api/common/apiInstance';

const AuthCallback = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const accessToken = searchParams.get('accessToken');
        console.log('Client side - accessToken:', accessToken);

        const response = await internalApi.get(
          `/auth/callback?accessToken=${accessToken}`,
        );

        if (response.status === 'OK') {
          router.push(response.redirectUrl);
        }
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
