import { externalApi } from './common/apiInstance';

// 로그아웃
export const logout = async () => {
  await externalApi.post('/auth/logout');
};
