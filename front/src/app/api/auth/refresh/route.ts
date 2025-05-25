import { NextRequest, NextResponse } from 'next/server';
import { removeCookie, setCookie } from '@/actions/auth';
import { api } from '@/api/common/apiInstance';
import { TokenResponse } from '@/types/auth';

// 토큰 갱신
export async function POST(request: NextRequest) {
  try {
    const { retryCount } = await request.json();

    const response = await api.post<TokenResponse>('/auth/refresh', {
      retryCount: retryCount,
      headers: {
        Cookie: request.headers.get('cookie'),
      },
    });

    if (response.status === 'OK') {
      const accessToken = response.data.accessToken;
      setCookie('accessToken', accessToken);

      return NextResponse.json(
        { message: response.message, data: accessToken },
        { status: 200 },
      );
    }

    removeCookie('accessToken');
    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    removeCookie('accessToken');
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
