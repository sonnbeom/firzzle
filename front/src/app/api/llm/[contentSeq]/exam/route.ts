import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';
import {
  NewExamChatResponse,
  SendExamChatRequest,
  SendExamChatResponse,
} from '@/types/learningChat';

// 시험모드 새질문 생성
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  const contentSeq = (await params).contentSeq;

  try {
    const response = await api.get<NewExamChatResponse>(
      `/llm/${contentSeq}/exam`,
    );

    if (response.status === 'OK') {
      return NextResponse.json(
        { message: response.message, data: response.data },
        { status: 200 },
      );
    }
    return NextResponse.json({ message: response.message }, { status: 500 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}

// 시험모드 채팅 전송
export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  const contentSeq = (await params).contentSeq;
  const { answer } = await request.json();

  try {
    const response = await api.post<SendExamChatResponse, SendExamChatRequest>(
      `/llm/${contentSeq}/exam`,
      {
        body: { answer },
      },
    );

    if (response.status === 'OK') {
      return NextResponse.json(
        { message: response.message, data: response.data },
        { status: 200 },
      );
    }

    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    return NextResponse.json(
      { message: (error as Error).message },
      { status: 500 },
    );
  }
}
