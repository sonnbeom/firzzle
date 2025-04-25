import DetailHeader from '@/components/common/TabHeader';
import VideoFrame from '@/components/common/VideoFrame';
import LearningChatContent from '@/components/learningChat/LearningChatContent';

const DetailLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className='flex h-full w-full gap-5'>
      <div className='flex flex-3 flex-col gap-6'>
        {/* <SideMenu /> */}
        {/* 영상 */}
        <VideoFrame />
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
