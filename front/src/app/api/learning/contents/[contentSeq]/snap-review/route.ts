import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const response = await api.get(
      `/learning/contents/${contentSeq}/snap-review`,
    );
    if (response.status === 'OK') {
      return NextResponse.json(response, { status: 200 });
    }
    return NextResponse.json(
      { message: '스냅리뷰를 가져오는데 실패했습니다.' },
      { status: 400 }
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const body = await request.json();
    const response = await api.patch(
      `/learning/contents/${contentSeq}/snap-review`,
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
