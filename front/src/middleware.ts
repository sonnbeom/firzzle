import { NextResponse, NextRequest } from 'next/server';

export async function middleware(request: NextRequest) {
  const cookieHeader = request.headers.get('cookie');
  // 전체 쿠키 헤더 로깅
  console.log('전체 쿠키 헤더:', cookieHeader);

  // 개별 쿠키 값들 로깅
  const cookies = request.cookies;
  console.log('쿠키 목록:');
  cookies.getAll().forEach((cookie) => {
    console.log(`- ${cookie.name}: ${cookie.value}`);
  });

  const accessToken = request.cookies.get('accessToken')?.value;

  const { pathname } = request.nextUrl;

  // accessToken이 없고 루트 경로가 아닌 경우 루트로 리다이렉트
  if (!accessToken && pathname !== '/' && !pathname.startsWith('/share')) {
    return NextResponse.redirect(new URL('/', request.url));
  }

  // accessToken이 있고 루트 경로인 경우 /content로 리다이렉트
  if (accessToken && pathname === '/') {
    return NextResponse.redirect(new URL('/content', request.url));
  }

  return NextResponse.next();
}

// 미들웨어가 적용될 경로 설정
export const config = {
  matcher: ['/', '/content/:path*', '/mylearning/:path*'],
};
