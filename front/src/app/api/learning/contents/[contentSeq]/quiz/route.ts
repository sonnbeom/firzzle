import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const response = await api.get(
      `/learning/contents/${(await params).contentSeq}/quiz`,
    );
    if (response.status === 'OK') {
      return NextResponse.json(response);
    }
    return NextResponse.json(
      { message: '퀴즈 데이터를 가져오는데 실패했습니다.' },
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
    const body = await request.json();
    console.log(body);
    const response = await api.post(
      `/learning/contents/${(await params).contentSeq}/quiz`,
      { body },
    );

    if (response.status === 'OK') {
      return NextResponse.json(response.data, { status: 200 });
    }

    return NextResponse.json(
      { message: '퀴즈 제출에 실패하였습니다.' },
      { status: 400 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
