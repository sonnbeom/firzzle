import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';
import {
  SendLearningChatRequest,
  SendLearningChatResponse,
} from '@/types/learningChat';

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
