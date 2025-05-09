import { cookies } from 'next/headers';

// 쿠키 조회
export const getServerCookie = async (name: string) => {
  const cookieStore = await cookies();
  return cookieStore.get(name);
};

// 쿠키 삭제
export const removeServerCookie = async (name: string) => {
  (await cookies()).delete(name);
};

// 쿠키 설정
export const setServerCookie = async (name: string, value: string) => {
  const cookieStore = await cookies();
  cookieStore.set({
    name: name,
    value: value,
    httpOnly: true,
    path: '/',
  });
};
