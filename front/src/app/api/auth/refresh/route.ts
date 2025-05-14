import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

// 토큰 갱신
export async function POST(request: NextRequest) {
  try {
    const { retryCount } = await request.json();

    const response = await api.post('/auth/refresh', {
      retryCount: retryCount,
    });

    if (response.status === 'OK') {
      return NextResponse.json({ message: response.message }, { status: 200 });
    }

    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
