import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function POST(request: NextRequest) {
  try {
    const response = await api.post('/learning/logging/visit');
    if (response.status === 'OK') {
      return NextResponse.json(response.data, { status: 200 });
    }
    return NextResponse.json(
      { message: '방문 로깅에 실패하였습니다.' },
      { status: 400 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
