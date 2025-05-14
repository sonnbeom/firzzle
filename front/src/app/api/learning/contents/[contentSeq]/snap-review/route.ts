import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const response = await api.get(
      `/learning/contents/${(await params).contentSeq}/snap-review`,
    );
    return NextResponse.json(response);
  } catch (error) {
    return NextResponse.json(
      { message: '스냅리뷰 조회에 실패하였습니다.' },
      { status: 500 },
    );
  }
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const body = await request.json();
    const response = await api.patch(
      `/learning/contents/${(await params).contentSeq}/snap-review`,
      { body },
    );

    if (response.status === 'OK') {
      return NextResponse.json(response, { status: 200 });
    }

    return NextResponse.json(
      { message: '프레임 설명 수정에 실패하였습니다.' },
      { status: 400 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
