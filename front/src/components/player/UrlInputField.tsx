'use client';

import { debounce } from 'lodash';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { postContent } from '@/api/content';
import { getPlayer } from '@/services/youtubeService';
import { PlayerInfo } from '@/types/player';
import BasicButton from '../common/BasicButton';
import BasicToaster from '../common/BasicToaster';
import SearchBar from '../common/SearchBar';

interface UrlInputFieldProps {
  defaultUrl?: string;
  setIsSubmitted: (isSubmitted: boolean) => void;
  setPlayerInfo: (playerInfo: PlayerInfo) => void;
  setTaskId: (taskId: string) => void;
}

const UrlInputField = ({
  defaultUrl = '',
  setIsSubmitted,
  setPlayerInfo,
  setTaskId,
}: UrlInputFieldProps) => {
  const [value, setValue] = useState(defaultUrl);
  const router = useRouter();

  // 입력된 url 영상 정보 조회
  const handleUrlSubmit = debounce(async (url: string) => {
    if (url.trim().length > 0) {
      const playerInfo = await getPlayer(url);
      setPlayerInfo(playerInfo);
    }
  }, 500);

  useEffect(() => {
    handleUrlSubmit(value);
    return () => {
      handleUrlSubmit.cancel();
    };
  }, [value]);

  // 영상 분석 시작
  const handleUrlConfirm = async () => {
    setIsSubmitted(true);
    try {
      const response = await postContent({ youtubeUrl: value });

      if (response.taskId) {
        setTaskId(response.taskId);
      } else {
        router.push(`/content/${response.contentSeq}`);
      }
    } catch (error) {
      setIsSubmitted(false);

      return BasicToaster.error(error.message, {
        id: 'analyze youtube',
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
