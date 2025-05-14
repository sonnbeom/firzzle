import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export async function middleware(request: NextRequest) {
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

  // API 요청인지 확인 (경로가 /api로 시작하거나 특정 패턴을 따르는지)
  // 예: /api/users, /api/auth/login 등
  if (request.nextUrl.pathname.startsWith('/api/')) {
    // 원래 요청의 URL에서 '/api'를 제거하고 백엔드 URL로 변경
    const backendUrl = new URL(
      request.nextUrl.pathname.replace(/^\/api/, ''),
      process.env.NEXT_PUBLIC_API_BASE_URL,
    );

    // 쿼리 파라미터 복사
    request.nextUrl.searchParams.forEach((value, key) => {
      backendUrl.searchParams.set(key, value);
    });

    // 요청 메서드, 헤더, 본문을 그대로 유지하며 새 요청 생성
    const requestHeaders = new Headers(request.headers);

    // 필요한 헤더 추가 또는 수정
    // requestHeaders.set('x-from-middleware', '1');

    // 요청 옵션 설정
    const options = {
      method: request.method,
      headers: requestHeaders,
    };

    try {
      // 백엔드로 요청 전송
      const response = await fetch(backendUrl, options);

      // 응답 데이터 가져오기
      const data = await response.text();

      // 응답 헤더 설정
      const responseHeaders = new Headers();
      response.headers.forEach((value, key) => {
        responseHeaders.set(key, value);
      });

      // 클라이언트에게 응답 반환
      return new NextResponse(data, {
        status: response.status,
        statusText: response.statusText,
        headers: responseHeaders,
      });
    } catch (error) {
      console.error('API 프록시 오류:', error);
      return new NextResponse(
        JSON.stringify({ error: 'API 요청 처리 중 오류가 발생했습니다.' }),
        {
          status: 500,
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );
    }
  }

  return NextResponse.next();
}

// 미들웨어가 적용될 경로 설정
export const config = {
  matcher: ['/', '/content/:path*', '/mylearning/:path*', '/api/:path*'],
};
