'use client';

import { useState } from 'react';
import PlayerFetcher from '@/components/player/PlayerFetcher';
import ProgressBar from '@/components/player/ProgressBar';
import { PlayerInfo } from '@/types/player';
import CopyrightInfo from './CopyrightInfo';
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

  const [taskId, setTaskId] = useState<string | null>(null);

  return (
    <div className='flex h-full w-full flex-col items-center justify-around gap-5 lg:gap-10'>
      {!playerInfo ? (
        <div className='flex flex-col items-center gap-2'>
          <p className='text-xl font-semibold whitespace-nowrap text-gray-950 md:text-2xl lg:text-3xl'>
            오늘은 어떤 영상을 학습할까요?
          </p>
          <p className='text-center font-medium whitespace-pre-wrap text-gray-700 md:text-lg'>
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
            setTaskId={setTaskId}
          />
        ) : (
          <ProgressBar taskId={taskId} setIsSubmitted={setIsSubmitted} />
        )}
      </div>

      <CopyrightInfo />
    </div>
  );
};

export default PlayerContent;
