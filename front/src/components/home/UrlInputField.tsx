'use client';

import { debounce } from 'lodash';
import { ChangeEvent, useEffect, useState } from 'react';
import { getPlayer } from '@/api/player';
import { PlayerInfo } from '@/types/player';
import BasicButton from '../common/BasicButton';
import Icons from '../common/Icons';

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

  const handleUrlChange = (e: ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value);
  };

  return (
    <div className='flex w-full items-center gap-4'>
      <div className='flex flex-1 items-center gap-3 rounded border border-gray-200 px-3 py-2'>
        <Icons id='search' />
        <input
          value={value}
          onChange={handleUrlChange}
          placeholder='학습할 영상의 링크를 입력하세요.'
          className='w-full text-lg focus:outline-none'
        />
      </div>

      <BasicButton
        isDisabled={value === ''}
        title='확인'
        onClick={() => setIsSubmitted(true)}
      />
    </div>
  );
};

export default UrlInputField;
