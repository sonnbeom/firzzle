import { NextResponse } from 'next/server';
import { ApiResponseError, ApiResponseWithoutData } from '@/types/common';
import { setServerCookie } from '../cookies';

// OAuth 인증 콜백
export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const accessToken = searchParams.get('accessToken');

  // accessToken이 없을 경우 에러 처리
  if (!accessToken) {
    return NextResponse.json({
      status: 'FAIL',
      cause: 'NOT_FOUND',
      message: 'ACCESS TOKEN이 없습니다.',
      prevUrl: request.url,
      redirectUrl: '/',
      data: null,
    } as ApiResponseError);
  }

  // 쿠키에 accessToken 저장
  await setServerCookie('accessToken', 'Bearer ' + accessToken);

  // 응답에서 토큰 제외
  return NextResponse.json({
    status: 'OK',
    cause: '',
    message: 'OAuth 인증 콜백 성공',
    prevUrl: request.url,
    redirectUrl: '/content',
    data: null,
  } as ApiResponseWithoutData);
}
