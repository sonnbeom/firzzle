import { removeCookie } from '@/actions/auth';
import { api } from './common/apiInstance';

// 토큰 갱신
export const refreshToken = async (retryCount: number) => {
  return await api.post('/auth/refresh', {
    retryCount: retryCount + 1,
  });
};

// 로그아웃
export const logout = async () => {
  try {
    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
    });

    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(`Logout failed: ${errorData}`);
    }

    removeCookie('accessToken');
  } catch (error) {
    console.error('Logout error:', error);
    throw error;
  }
};
