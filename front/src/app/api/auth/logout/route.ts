import { NextRequest, NextResponse } from 'next/server';
import { removeCookie } from '@/actions/auth';
import { api } from '@/api/common/apiInstance';

// 로그아웃
export async function POST(request: NextRequest) {
  try {
    console.log('로그아웃 API 라우트 쿠키', request.headers.get('cookie'));

    const response = await api.post('/auth/logout', {
      headers: {
        Cookie: request.headers.get('cookie'),
      },
    });

    if (response.status === 'OK') {
      removeCookie('accessToken');

      const response = NextResponse.json(
        { message: '로그아웃 되었습니다.' },
        { status: 200 },
      );

      response.cookies.delete('accessToken');
      response.cookies.delete('refresh_token');

      return response;
    }

    return NextResponse.json(
      { message: '로그아웃에 실패하였습니다.' },
      { status: 400 },
    );
  } catch (error) {
    console.log(error);

    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
