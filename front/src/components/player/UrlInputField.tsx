'use client';

import { debounce } from 'lodash';
import { useEffect, useState } from 'react';
import { postContent } from '@/api/content';
import { getYouTubeVideoInfo } from '@/services/youtubeServices';
import { PlayerInfo } from '@/types/player';
import BasicButton from '../common/BasicButton';
import BasicToaster from '../common/BasicToaster';
import SearchBar from '../common/SearchBar';

interface UrlInputFieldProps {
  defaultUrl?: string;
  setIsSubmitted: (isSubmitted: boolean) => void;
  setPlayerInfo: (playerInfo: PlayerInfo) => void;
}

const UrlInputField = ({
  defaultUrl = '',
  setIsSubmitted,
  setPlayerInfo,
}: UrlInputFieldProps) => {
  const [value, setValue] = useState(defaultUrl);

  // 입력된 url 영상 정보 조회
  const handleUrlSubmit = debounce(async (url: string) => {
    if (url.trim().length > 0) {
      try {
        const videoInfo = await getYouTubeVideoInfo(url);
        if (videoInfo) {
          setPlayerInfo(videoInfo);
        }
      } catch (error) {
        setPlayerInfo(null);
        return BasicToaster.error(error.message, {
          id: 'fetch youtube',
          duration: 3000,
        });
      }
    }
  }, 500);

  useEffect(() => {
    handleUrlSubmit(value);
    return () => {
      handleUrlSubmit.cancel();
    };
  }, [value, handleUrlSubmit]);

  // 영상 분석 시작
  const handleUrlConfirm = async () => {
    setIsSubmitted(true);
    try {
      await postContent();
    } catch (error) {
      return BasicToaster.error(error.message, {
        id: 'fetch youtube',
        duration: 2000,
      });
    }
  };

  return (
    <div className='flex h-full w-full items-center gap-2 lg:gap-4'>
      <SearchBar
        value={value}
        placeholder='학습할 영상의 링크를 입력하세요.'
        onChange={(e) => {
          setValue(e.target.value);
        }}
        onSubmit={handleUrlConfirm}
        hasSubmitButton={false}
      />

      <BasicButton
        isDisabled={value === ''}
        title='확인'
        onClick={handleUrlConfirm}
        className='h-full'
      />
    </div>
  );
};

export default UrlInputField;
