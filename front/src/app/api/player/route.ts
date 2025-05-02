import { NextResponse } from 'next/server';

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const url = searchParams.get('url');

  try {
    // YouTube id 추출
    const playerId = url.match(
      /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/,
    )?.[1];

    if (!playerId) {
      return NextResponse.json(
        { error: 'Invalid YouTube URL' },
        { status: 400 },
      );
    }

    // YouTube oEmbed API 호출
    const response = await fetch(
      `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${playerId}&format=json`,
    );

    if (!response.ok) {
      throw new Error('YouTube oEmbed API 응답 에러');
    }

    const data = await response.json();

    return NextResponse.json({
      playerId,
      title: data.title,
    });
  } catch (error) {
    console.error('YouTube API oEmbed 조회 실패:', error);
    return NextResponse.json(
      { error: 'YouTube API oEmbed 조회 실패' },
      { status: 500 },
    );
  }
}
