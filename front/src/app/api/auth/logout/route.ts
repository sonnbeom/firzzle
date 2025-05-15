import { NextRequest, NextResponse } from 'next/server';
import { removeCookie } from '@/actions/auth';
import { api } from '@/api/common/apiInstance';

// 로그아웃
export async function POST(request: NextRequest) {
  try {
    console.log('쿠키 헤더: ', request.headers.get('Cookie'));

    const response = await api.post('/auth/logout', {
      headers: {
        Cookie: request.headers.get('cookie'),
      },
    });

    if (response.status === 'OK') {
      removeCookie('accessToken');
      request.cookies.delete('accessToken');

      return NextResponse.json(
        { message: '로그아웃 되었습니다.' },
        { status: 200 },
      );
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
