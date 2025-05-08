'use client';

import { debounce } from 'lodash';
import { useEffect, useState } from 'react';
import { postContent } from '@/api/content';
import { getPlayer } from '@/api/player';
import { PlayerInfo } from '@/types/player';
import BasicButton from '../common/BasicButton';
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

  useEffect(
    debounce(() => {
      const handleUrlSubmit = async (url: string) => {
        const { data } = await getPlayer(url);
        setPlayerInfo(data);
      };

      if (value.trim().length > 0) {
        handleUrlSubmit(value);
      }
    }, 500),
    [value],
  );

  const handleUrlConfirm = async () => {
    setIsSubmitted(true);
    try {
      await postContent();
    } catch (error) {
      console.error('컨텐츠 등록 실패:', error);
    }
  };

  return (
    <div className='flex w-full items-center gap-2 lg:gap-4'>
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
      />
    </div>
  );
};

export default UrlInputField;
