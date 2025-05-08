'use client';

import PlayerFrame from './PlayerFrame';

interface PlayerFetcherProps {
  playerId: string;
  title: string;
}

const PlayerFetcher = ({ playerId, title }: PlayerFetcherProps) => {
  return (
    <div className='flex w-full flex-col items-center gap-5 md:w-[600px] lg:w-[800px] lg:gap-10'>
      <p className='line-clamp-2 text-center text-lg font-semibold text-gray-900 md:text-xl lg:text-2xl'>
        {title}
      </p>
      <PlayerFrame playerId={playerId} />
    </div>
  );
};

export default PlayerFetcher;
