'use client';

import PlayerFrame from '../common/PlayerFrame';

interface PlayerFetcherProps {
  playerId: string;
  title: string;
}

const PlayerFetcher = ({ playerId, title }: PlayerFetcherProps) => {
  return (
    <div className='flex max-w-[800px] flex-col items-center gap-10'>
      <p className='line-clamp-2 text-center text-2xl font-semibold text-gray-900'>
        {title}
      </p>
      <PlayerFrame playerId={playerId} />
    </div>
  );
};

export default PlayerFetcher;
