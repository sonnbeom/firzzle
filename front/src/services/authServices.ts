import { setCookie } from '@/actions/auth';
import BasicToaster from '@/components/common/BasicToaster';

export const login = async (accessToken: string) => {
  if (accessToken) {
    setCookie('accessToken', accessToken);
    return '/content';
  } else {
    BasicToaster.error('로그인에 실패했습니다.');
    return '/';
  }
};
