import BasicToaster from '@/components/common/BasicToaster';
import PlayerContent from '@/components/player/PlayerContent';
import { getYouTubeVideoInfo } from '@/services/youtubeServices';
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
      initialPlayerInfo = await getYouTubeVideoInfo(url);
    } catch (error) {
      return BasicToaster.error(error.message, { duration: 2000 });
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
