'use client';

import { useState } from 'react';
import PlayerFetcher from '@/components/player/PlayerFetcher';
import ProgressBar from '@/components/player/ProgressBar';
import { PlayerInfo } from '@/types/player';
import UrlInputField from './UrlInputField';

interface PlayerContentProps {
  initialPlayerInfo: PlayerInfo | null;
  initialUrl: string;
}

const PlayerContent = ({
  initialPlayerInfo,
  initialUrl,
}: PlayerContentProps) => {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [playerInfo, setPlayerInfo] = useState<PlayerInfo | null>(
    initialPlayerInfo,
  );

  return (
    <div className='flex w-full flex-col items-center gap-10'>
      {!playerInfo ? (
        <div className='mt-50 flex flex-col items-center gap-2'>
          <p className='text-4xl font-semibold text-gray-900'>
            오늘은 어떤 영상을 학습할까요?
          </p>
          <p className='text-lg font-medium text-gray-900'>
            YouTube, Vimeo 등 다양한 플랫폼의 영상 링크를 입력하세요.
          </p>
        </div>
      ) : (
        <PlayerFetcher
          playerId={playerInfo.playerId}
          title={playerInfo.title}
        />
      )}

      <div className='flex w-[800px] flex-col items-center gap-10'>
        {!isSubmitted ? (
          <UrlInputField
            defaultUrl={initialUrl}
            setIsSubmitted={setIsSubmitted}
            setPlayerInfo={setPlayerInfo}
          />
        ) : (
          <ProgressBar />
        )}
      </div>
    </div>
  );
};

export default PlayerContent;
