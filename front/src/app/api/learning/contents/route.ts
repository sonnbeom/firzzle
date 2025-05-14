import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

// 학습 컨텐츠 분석
export async function POST(request: NextRequest) {
  try {
    const response = await api.post('/learning/contents');

    if (response.status === 'OK') {
      return NextResponse.json({ message: response.message }, { status: 200 });
    }

    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
