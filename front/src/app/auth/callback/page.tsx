'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';
import { login } from '@/services/authServices';

const AuthCallback = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const handleCallback = async () => {
      const accessToken = searchParams.get('accessToken');
      const redirectUrl = await login(accessToken);

      router.replace(redirectUrl);
    };

    handleCallback();
  }, []);

  return null;
};

export default AuthCallback;
