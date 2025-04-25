import DetailHeader from '@/components/common/TabHeader';
import LearningChatContent from '@/components/learningChat/LearningChatContent';

const DetailLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className='flex h-full w-full gap-5'>
      <div className='flex flex-3 flex-col gap-6'>
        {/* <SideMenu /> */}
        <div className='relative aspect-video w-full'>
          {/* 유튜브 영상 */}
          <iframe
            className='absolute top-0 left-0 h-full w-full'
            src='https://www.youtube.com/embed/VIDEO_ID'
            title='YouTube video player'
            allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture'
            allowFullScreen
          />
        </div>
        {/* 러닝챗 */}
        <LearningChatContent />
      </div>
      <div className='flex flex-7 flex-col items-center gap-4'>
        <DetailHeader />
        {children}
      </div>
    </div>
  );
};

export default DetailLayout;
