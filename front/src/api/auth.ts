// 로그아웃
export const logout = async () => {
  const response = await fetch('/api/auth/logout', {
    method: 'POST',
  });

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.message;
};
