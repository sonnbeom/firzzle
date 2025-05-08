import { TokenResponse } from '@/types/auth';
import { setCookie } from '@/utils/auth';
import { externalApi } from './common/apiInstance';

// 토큰 갱신
export const refresh = async () => {
  const { data } = await externalApi.post<TokenResponse>('/auth/refresh');
  console.log('accessToken: ', data.accessToken);
  setCookie('accessToken', data.accessToken);
  return data;
};
