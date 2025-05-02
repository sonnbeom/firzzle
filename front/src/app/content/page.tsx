import { getPlayer } from '@/api/player';
import PlayerContent from '@/components/player/PlayerContent';
import { PlayerInfo } from '@/types/player';

const ContentPage = async ({
  searchParams,
}: {
  searchParams: Promise<{ url?: string }>;
}) => {
  const { url } = await searchParams;
  let initialPlayerInfo: PlayerInfo | null = null;

  if (url) {
    try {
      const { data } = await getPlayer(url);
      initialPlayerInfo = data;
    } catch (error) {
      console.error('플레이어 조회 실패:', error);
    }
  }

  return (
    <PlayerContent
      initialPlayerInfo={initialPlayerInfo}
      initialUrl={url || ''}
    />
  );
};

export default ContentPage;
