import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const response = await api.get(
      `/learning/contents/${contentSeq}/share-code`,
    );
    if (response.status === 'OK') {
      return NextResponse.json(response, { status: 200 });
    }
    return NextResponse.json(
      { message: '공유 코드를 가져오는데 실패했습니다.' },
      { status: 400 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const response = await api.post(
      `/learning/contents/${contentSeq}/snap-review/share`,
    );
    if (response.status === 'OK') {
      return NextResponse.json(response, { status: 200 });
    }
    return NextResponse.json(
      { message: '공유 코드 생성에 실패했습니다.' },
      { status: 400 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
