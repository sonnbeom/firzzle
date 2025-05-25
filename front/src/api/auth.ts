import BasicToaster from '@/components/common/BasicToaster';
import { AdminLoginRequest, UserInfo } from '@/types/auth';

// 내정보 조회
export const getMyInfo = async (): Promise<UserInfo> => {
  const response = await fetch('/api/auth/me');
  const data = await response.json();

  if (response.status === 200) {
    return data;
  } else {
    BasicToaster.error(data.message, { id: 'my-info' });
    return data;
  }
};

// 로그아웃
export const logout = async () => {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'include',
  });

  const data = await response.json();

  if (response.status === 200) {
    window.location.href = '/';
  } else {
    BasicToaster.error(data.message, { id: 'logout' });
  }
};

// 관리자 로그인
export const adminLogin = async (request: AdminLoginRequest) => {
  const response = await fetch('/api/auth/admin', {
    method: 'POST',
    body: JSON.stringify(request),
  });

  const data = await response.json();

  if (response.status === 200) {
    window.location.href = '/admin/strategyboard';
  } else {
    BasicToaster.error(data.message, { id: 'admin-login' });
  }
};
