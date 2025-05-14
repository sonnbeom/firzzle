import { getCookie, removeCookie } from '@/actions/auth';
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
    const accessToken = (await getCookie('accessToken')).value;

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL}/auth/logout`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        mode: 'cors',
        credentials: 'include',
      },
    );

    console.log(response);

    if (response.status === 200) {
      removeCookie('accessToken');
    } else {
      throw response.statusText;
    }
  } catch (error) {
    throw error;
  }
};
