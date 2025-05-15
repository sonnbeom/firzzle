import BasicToaster from '@/components/common/BasicToaster';

// 로그아웃
export const logout = async () => {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'include',
  });

  const data = await response.json();

  if (response.status !== 200) {
    BasicToaster.error(data.message, { id: 'logout', duration: 2000 });
  } else {
    BasicToaster.default(data.message, { id: 'logout', duration: 2000 });
    window.location.href = '/';
  }
};
