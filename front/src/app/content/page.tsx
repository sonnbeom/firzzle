import VideoFrame from '@/components/common/VideoFrame';
import UrlInputField from '@/components/home/UrlInputField';

const ContentPage = () => {
  return (
    <div className='flex w-full flex-col items-center gap-10'>
      <div className='flex flex-col items-center gap-2'>
        <p className='text-4xl font-semibold text-gray-900'>
          오늘은 어떤 영상을 학습할까요?
        </p>
        <p className='text-lg font-medium text-gray-900'>
          YouTube, Vimeo 등 다양한 플랫폼의 영상 링크를 입력하세요.
        </p>
      </div>

      <div className='flex w-[800px] flex-col items-center gap-10'>
        <VideoFrame />

        <UrlInputField />
      </div>
    </div>
  );
};

export default ContentPage;
