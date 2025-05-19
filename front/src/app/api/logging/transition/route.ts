// 컨텐츠 전환 로깅
import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';
import { TransitionLogRequest } from '@/types/log';

export async function POST(request: NextRequest) {
  try {
    const { fromContent, toContent } =
      (await request.json()) as TransitionLogRequest;

    const response = await api.post('/learning/logging/transition', {
      body: {
        fromContent,
        toContent,
      },
    });

    if (response.status === 'OK') {
      return NextResponse.json(
        { message: response.message, data: response.data },
        { status: 200 },
      );
    }

    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
