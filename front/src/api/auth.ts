import BasicToaster from '@/components/common/BasicToaster';

// 로그아웃
export const logout = async () => {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'include',
  });

  const data = await response.json();

  if (response.status === 200) {
    BasicToaster.default(data.message, { id: 'logout' });
    return true;
  } else {
    BasicToaster.error(data.message, { id: 'logout' });
    return false;
  }
};
