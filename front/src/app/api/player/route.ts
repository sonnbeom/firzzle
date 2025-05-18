import { NextResponse } from 'next/server';
import { PlayerInfo } from '@/types/player';
import { getYouTubeVideoId } from '@/utils/youtube';
// 플레이어 정보 조회
export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const url = searchParams.get('url');

  console.log(url);
  try {
    // YouTube id 추출
    const playerId = getYouTubeVideoId(url);

    if (!playerId) {
      return NextResponse.json(
        { error: '유효하지 않은 URL 형식입니다.' },
        { status: 400 },
      );
    }

    // YouTube oEmbed API 호출
    const response = await fetch(
      `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${playerId}&format=json`,
    );

    if (!response.ok) {
      throw new Error(`YouTube API 에러: ${response.status}`);
    }

    const oembedData = await response.json();

    return NextResponse.json(
      { data: { playerId, title: oembedData.title } as PlayerInfo },
      { status: 200 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
