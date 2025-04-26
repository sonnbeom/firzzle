'use client';

import { useState } from 'react';
import VideoFrame from '@/components/common/VideoFrame';
import UrlInputField from '@/components/home/UrlInputField';

const ContentPage = () => {
  const [isSubmitted, setIsSubmitted] = useState(false);

  return (
    <div className='flex w-full flex-col items-center gap-10'>
      {!isSubmitted ? (
        <div className='flex flex-col items-center gap-2'>
          <p className='text-4xl font-semibold text-gray-900'>
            오늘은 어떤 영상을 학습할까요?
          </p>
          <p className='text-lg font-medium text-gray-900'>
            YouTube, Vimeo 등 다양한 플랫폼의 영상 링크를 입력하세요.
          </p>
        </div>
      ) : (
        <p className='line-clamp-2 w-[800px] text-center text-2xl font-semibold text-gray-900'>
          AI 딥러닝, 머신러닝 초간단 인공지능 개념정리
        </p>
      )}

      <div className='flex w-[800px] flex-col items-center gap-10'>
        <VideoFrame />
        {!isSubmitted ? (
          <UrlInputField />
        ) : (
          <div className='flex flex-col items-center text-lg font-medium text-gray-900'>
            <p>입력하신 영상을 학습 자료로 분석 중이에요</p>
            <p>약 10분 정도 소요될 수 있어요</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ContentPage;
