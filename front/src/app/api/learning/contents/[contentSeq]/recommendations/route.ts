import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const type = searchParams.get('type');
    const contentSeq = (await params).contentSeq;

    // type이 'expert'이면 전문가 추천, 아니면 일반 추천
    const endpoint =
      type === 'expert' ? 'expert-recommendations' : 'recommendations';

    const response = await api.get(
      `/learning/contents/${contentSeq}/${endpoint}`,
      {
        params: {
          p_pageno: searchParams.get('p_pageno'),
          p_pagesize: searchParams.get('p_pagesize'),
          p_order: searchParams.get('p_order'),
          p_sortorder: searchParams.get('p_sortorder'),
          keyword: searchParams.get('keyword'),
          category: searchParams.get('category'),
          status: searchParams.get('status'),
        },
      },
    );

    if (response.status === 'OK') {
      return NextResponse.json(response.data, { status: 200 });
    }

    const errorMessage =
      type === 'expert'
        ? '전문가 추천을 가져오는데 실패했습니다.'
        : '추천 강의를 가져오는데 실패했습니다.';

    return NextResponse.json({ message: errorMessage }, { status: 400 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
