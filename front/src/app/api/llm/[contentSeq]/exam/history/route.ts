import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';
import { GetExamChatHistoryResponse } from '@/types/learningChat';

// 시험모드 채팅 기록 조회
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ contentSeq: string }> },
) {
  try {
    const contentSeq = (await params).contentSeq;
    const searchParams = request.nextUrl.searchParams;
    const lastIndate = searchParams.get('lastIndate') || undefined;

    const apiUrl = lastIndate
      ? `/llm/${contentSeq}/exam/history?lastIndate=${lastIndate}`
      : `/llm/${contentSeq}/exam/history`;

    const response = await api.get<GetExamChatHistoryResponse>(apiUrl);

    if (response.status === 'OK') {
      return NextResponse.json(
        {
          message: response.message,
          info: response.data.info,
          data: response.data.historyList,
        },
        { status: 200 },
      );
    } else {
      return NextResponse.json({ message: response.message }, { status: 400 });
    }
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
