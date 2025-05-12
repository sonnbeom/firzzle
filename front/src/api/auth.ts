import { externalApi } from './common/apiInstance';

// 로그아웃
export const logout = async () => {
  const response = await externalApi.post('/auth/logout');
  console.log('로그아웃 응답: ', response);
  return response;
};
