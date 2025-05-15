'use server';

import { cookies } from 'next/headers';

// 쿠키 조회
export const getCookie = async (name: string) => {
  const cookieStore = await cookies();
  return cookieStore.get(name).value;
};

// 쿠키 삭제
export const removeCookie = async (name: string) => {
  (await cookies()).delete(name);
};

// 쿠키 설정
export const setCookie = async (name: string, value: string) => {
  const cookieStore = await cookies();
  cookieStore.set({
    name: name,
    value: value,
    httpOnly: true,
    secure: true,
    sameSite: 'strict',
    path: '/',
  });
};

// 모든 쿠키 조회
export const getAllCookies = async () => {
  const cookieStore = await cookies();
  return cookieStore.getAll();
};
