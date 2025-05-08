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
    <div className='flex w-full flex-col items-center gap-5 lg:gap-10'>
      {!playerInfo ? (
        <div className='mt-60 flex flex-col items-center gap-1 lg:mt-50 lg:gap-2'>
          <p className='text-2xl font-semibold whitespace-nowrap text-gray-900 md:text-3xl'>
            오늘은 어떤 영상을 학습할까요?
          </p>
          <p className='font-medium whitespace-nowrap text-gray-900 md:text-lg'>
            YouTube, Vimeo 등 다양한 플랫폼의 영상 링크를 입력하세요.
          </p>
        </div>
      ) : (
        <PlayerFetcher
          playerId={playerInfo.playerId}
          title={playerInfo.title}
        />
      )}

      <div className='w-full md:w-[700px] lg:w-[800px]'>
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
