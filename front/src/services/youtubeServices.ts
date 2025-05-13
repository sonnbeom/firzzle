import { PlayerInfo } from '@/types/player';
import { getYouTubeVideoId } from '@/utils/youtube';

/**
 * YouTube 영상 정보를 가져오는 함수
 * @param url YouTube URL
 * @returns PlayerInfo 또는 null
 */
export const getYouTubeVideoInfo = async (
  url: string,
): Promise<PlayerInfo | null> => {
  try {
    // YouTube id 추출
    const playerId = getYouTubeVideoId(url);

    if (!playerId) {
      throw new Error('유효하지 않은 YouTube URL입니다.');
    }

    // YouTube oEmbed API 호출
    const response = await fetch(
      `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${playerId}&format=json`,
    );

    if (!response.ok) {
      throw new Error('YouTube API 호출 실패');
    }

    const oembedData = await response.json();

    return {
      playerId,
      title: oembedData.title,
    };
  } catch (error) {
    throw error;
  }
};
