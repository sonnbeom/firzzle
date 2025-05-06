import { NextRequest, NextResponse } from 'next/server';
import { BASE_URL } from '@/utils/const';

export async function GET(request: NextRequest) {
  // URL 파라미터 추출
  const { searchParams } = new URL(request.url);

  try {
    const apiUrl = `${BASE_URL}/learning/contents${
      searchParams.toString() ? `?${searchParams.toString()}` : ''
    }`;

    const response = await fetch(apiUrl, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error('API 요청 실패');
    }

    const data = await response.json();
    console.log('학습 내역 목록 조회: ', data);
    return NextResponse.json(data);
  } catch (error) {
    console.error('API 요청 실패:', error);
    return NextResponse.json(
      { error: '컨텐츠를 가져오는 데 실패했습니다.' },
      { status: 500 },
    );
  }
}
