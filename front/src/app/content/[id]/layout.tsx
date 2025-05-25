import { ReactNode } from 'react';
import { getContent } from '@/api/content';
import DetailHeader from '@/components/common/DetailHeader';
import ChatHistoryLoading from '@/components/learningChat/ChatHistoryLoading';
import LearningChatContent from '@/components/learningChat/LearningChatContent';
import PlayerFrame from '@/components/player/PlayerFrame';
import CustomSuspense from '@/services/CutstomSuspense';

interface PageProps {
  params: Promise<{ id: string }>;
  children: ReactNode;
}

const DetailLayout = async ({ params, children }: PageProps) => {
  const { id } = await params;
  const data = await getContent(id);

  return (
    <div className='flex h-[87dvh] w-full gap-5'>
      <div className='hidden flex-2 flex-col gap-6 lg:flex xl:flex-3'>
        {/* 영상 */}
        <PlayerFrame playerId={data.videoId} />
        {/* 러닝챗 */}
        <CustomSuspense
          props={{
            fallback: <ChatHistoryLoading />,
            children: <LearningChatContent contentId={id} />,
          }}
        />
      </div>
      <div className='flex flex-3 flex-col items-center gap-2 lg:gap-4 xl:flex-7'>
        <DetailHeader />
        <div className='h-full w-full overflow-y-auto'>{children}</div>
      </div>
    </div>
  );
};

export default DetailLayout;
