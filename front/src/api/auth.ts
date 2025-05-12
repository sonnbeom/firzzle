import { externalApi } from './common/apiInstance';

// 토큰 갱신
export const refreshToken = async (retryCount: number) => {
  return await externalApi.post('/auth/refresh', {
    retryCount: retryCount + 1,
  });
};

// 로그아웃
export const logout = async () => {
  const response = await externalApi.post('/auth/logout');
  console.log('로그아웃 응답: ', response);
  return response;
};
