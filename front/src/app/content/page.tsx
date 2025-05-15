import PlayerContent from '@/components/player/PlayerContent';
import { fetchYouTubeVideoInfo } from '@/services/youtubeService';
import { PlayerInfo } from '@/types/player';

const ContentPage = async ({
  searchParams,
}: {
  searchParams: Promise<{ url?: string }>;
}) => {
  const { url } = await searchParams;
  let initialPlayerInfo: PlayerInfo | null = null;

  if (url) {
    const playerInfo = await fetchYouTubeVideoInfo(url);
    initialPlayerInfo = playerInfo;
  }

  return (
    <PlayerContent
      initialPlayerInfo={initialPlayerInfo}
      initialUrl={url || ''}
    />
  );
};

export default ContentPage;
