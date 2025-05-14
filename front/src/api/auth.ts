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
    const response = await api.post<undefined>('/auth/logout');

    if (response.status === 'OK') {
      removeCookie('accessToken');
    } else {
      throw response.message;
    }
  } catch (error) {
    throw error;
  }
};
