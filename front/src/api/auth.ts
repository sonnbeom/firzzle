import { getCookie, removeCookie } from '@/actions/auth';
import { api } from './common/apiInstance';

const accessToken = (await getCookie('accessToken')).value;

// 토큰 갱신
export const refreshToken = async (retryCount: number) => {
  return await api.post('/auth/refresh', {
    retryCount: retryCount + 1,
  });
};

// 로그아웃
export const logout = async () => {
  try {
    const response = await fetch('/auth/logout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json;charset=UTF-8',
        Authorization: `Bearer ${accessToken}`,
      },
    });

    if (response.status === 200) {
      removeCookie('accessToken');
    } else {
      throw response.statusText;
    }
  } catch (error) {
    throw error;
  }
};
