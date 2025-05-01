'use client';

import { useSearchParams } from 'next/navigation';
import { useState } from 'react';
import PlayerFetcher from '@/components/content/PlayerFetcher';
import ProgressBar from '@/components/content/ProgressBar';
import UrlInputField from '@/components/home/UrlInputField';

interface VideoInfo {
  videoId: string;
  title: string;
}

const ContentPage = () => {
  const searchParams = useSearchParams();
  const urlParam = searchParams.get('url');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [videoInfo, setVideoInfo] = useState<VideoInfo | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleUrlSubmit = async (url: string) => {
    setIsLoading(true);
    try {
      const response = await fetch(
        `/api/player?url=${encodeURIComponent(url)}`,
      );
      const data = await response.json();

      if (response.ok) {
        setVideoInfo(data);
      } else {
        console.error('Error:', data.error);
      }
    } catch (error) {
      console.error('Failed to fetch video info:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleConfirm = () => {
    setIsSubmitted(true);
  };

  return (
    <div className='flex w-full flex-col items-center gap-10'>
      {!videoInfo ? (
        <div className='flex flex-col items-center gap-2'>
          <p className='text-4xl font-semibold text-gray-900'>
            오늘은 어떤 영상을 학습할까요?
          </p>
          <p className='text-lg font-medium text-gray-900'>
            YouTube, Vimeo 등 다양한 플랫폼의 영상 링크를 입력하세요.
          </p>
        </div>
      ) : (
        <PlayerFetcher playerId={videoInfo.videoId} title={videoInfo.title} />
      )}

      <div className='flex w-[800px] flex-col items-center gap-10'>
        {!isSubmitted ? (
          <UrlInputField
            defaultUrl={urlParam || ''}
            onSubmit={handleUrlSubmit}
            onConfirm={handleConfirm}
            onClear={() => setVideoInfo(null)}
          />
        ) : (
          <ProgressBar />
        )}
      </div>
    </div>
  );
};

export default ContentPage;
