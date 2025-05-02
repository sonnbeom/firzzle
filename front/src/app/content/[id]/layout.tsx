import { ReactNode } from 'react';
import DetailHeader from '@/components/common/TabHeader';
import LearningChatContent from '@/components/learningChat/LearningChatContent';
import PlayerFrame from '@/components/player/PlayerFrame';

interface PageProps {
  params: Promise<{ id: string }>;
  children: ReactNode;
}

const DetailLayout = async ({ params, children }: PageProps) => {
  const { id } = await params;
  const playerId = 'dQw4w9WgXcQ';

  return (
    <div className='flex h-full w-full gap-5 overflow-hidden'>
      <div className='flex flex-3 flex-col gap-6 overflow-hidden'>
        {/* <SideMenu /> */}
        {/* 영상 */}
        <PlayerFrame playerId={playerId} />
        {/* 러닝챗 */}
        <LearningChatContent contentId={id} />
      </div>
      <div className='flex flex-7 flex-col items-center gap-4 overflow-hidden'>
        <DetailHeader />
        <div className='h-full w-full overflow-y-auto'>{children}</div>
      </div>
    </div>
  );
};

export default DetailLayout;
