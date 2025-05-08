import { NextResponse } from 'next/server';
import { TokenResponse } from '@/types/auth';
import { ApiResponseError, ApiResponseWithData } from '@/types/common';

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
  const response = NextResponse.json({
    status: 'OK',
    cause: '',
    message: 'OAuth 인증 콜백 성공',
    prevUrl: request.url,
    redirectUrl: '/content',
    data: {
      accessToken,
    } as TokenResponse,
  } as ApiResponseWithData<TokenResponse>);

  response.cookies.set('accessToken', accessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    path: '/',
  });

  return response;
}
