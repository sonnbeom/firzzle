import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';
import {
  GetLearningChatHistoryResponse,
  SendLearningChatRequest,
  SendLearningChatResponse,
} from '@/types/learningChat';

// 학습모드 채팅 전송
export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const { question } = await request.json();

    const response = await api.post<
      SendLearningChatResponse,
      SendLearningChatRequest
    >(`/llm/${contentSeq}/chat`, {
      body: { question },
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

// 학습모드 채팅 기록 조회
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const searchParams = request.nextUrl.searchParams;
    const lastIndate = searchParams.get('lastIndate') || undefined;

    const apiUrl = lastIndate
      ? `/llm/${contentSeq}/chat?lastIndate=${lastIndate}`
      : `/llm/${contentSeq}/chat`;

    const response = await api.get<GetLearningChatHistoryResponse>(apiUrl);

    if (response.status === 'OK') {
      return NextResponse.json(
        { message: response.message, data: response.data },
        { status: 200 },
      );
    } else {
      return NextResponse.json({ message: response.message }, { status: 400 });
    }
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
