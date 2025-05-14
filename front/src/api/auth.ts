import { api } from './common/apiInstance';

// 토큰 갱신
export const refreshToken = async (retryCount: number) => {
  return await api.post('/auth/refresh', {
    retryCount: retryCount + 1,
  });
};

// 로그아웃
export const logout = async () => {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
  });

  if (!response.ok) {
    throw new Error('로그아웃 중 오류가 발생했습니다.');
  }

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.message;
};
