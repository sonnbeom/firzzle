'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { login } from '@/services/authServices';

const AuthCallback = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const accessToken = searchParams.get('accessToken');

  login(accessToken).then((redirectUrl) => {
    router.replace(redirectUrl);
  });

  return null;
};

export default AuthCallback;
