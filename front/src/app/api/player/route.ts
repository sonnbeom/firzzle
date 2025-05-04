import { ApiResponseWithData, ApiResponseError } from '@/types/common';
import { PlayerInfo } from '@/types/player';
import { getYouTubeVideoId } from '@/utils/youtube';

// 플레이어 정보 조회
export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const url = searchParams.get('url');

  try {
    // YouTube id 추출
    const playerId = getYouTubeVideoId(url);

    if (!playerId) {
      return Response.json({
        status: 'FAIL',
        cause: 'INVALID_URL',
        message: 'YouTube URL이 유효하지 않습니다.',
        prevUrl: request.url,
        redirectUrl: '',
        data: null,
      } as ApiResponseError);
    }

    // YouTube oEmbed API 호출
    const response = await fetch(
      `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${playerId}&format=json`,
    );

    if (!response.ok) {
      throw new Error(`YouTube API 에러: ${response.status}`);
    }

    const oembedData = await response.json();

    return Response.json({
      status: 'OK',
      cause: '',
      message: 'YouTube API oEmbed 조회를 성공했습니다.',
      prevUrl: request.url,
      redirectUrl: '',
      data: {
        playerId,
        title: oembedData.title,
      } as PlayerInfo,
    } as ApiResponseWithData<PlayerInfo>);
  } catch (error) {
    console.log('YouTube API oEmbed 조회 실패: ', error);

    return Response.json({
      status: 'FAIL',
      cause: 'API_ERROR',
      message: '서버 오류가 발생했습니다.',
      prevUrl: request.url,
      redirectUrl: '',
      data: null,
    } as ApiResponseError);
  }
}
