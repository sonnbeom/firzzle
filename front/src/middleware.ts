import { NextResponse, NextRequest } from 'next/server';

export async function middleware(request: NextRequest) {
  const accessToken = request.cookies.get('accessToken')?.value;
  const { pathname } = request.nextUrl;
  const url = new URL(request.url);

  const isPublicPath = pathname === '/' || pathname.startsWith('/share');

  if (!accessToken && !isPublicPath) {
    url.pathname = '/';
    return NextResponse.redirect(url);
  }

  if (accessToken && pathname === '/') {
    url.pathname = '/content';
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

// 미들웨어가 적용될 경로 설정
export const config = {
  matcher: ['/', '/content/:path*', '/mylearning/:path*'],
};
