import { NextResponse } from 'next/server';
import { setServerCookie } from '../cookies';

// OAuth 인증 콜백
export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const accessToken = searchParams.get('accessToken');

  // accessToken이 없을 경우 에러 처리
  if (!accessToken) {
    return NextResponse.redirect(new URL('/', request.url));
  }

  // 쿠키에 accessToken 저장
  const response = NextResponse.redirect(new URL('/content', request.url));

  setServerCookie('accessToken', accessToken);

  return response;
}
