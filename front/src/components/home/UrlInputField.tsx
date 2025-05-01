'use client';

import { useState } from 'react';
import BasicButton from '../common/BasicButton';
import Icons from '../common/Icons';

interface UrlInputFieldProps {
  defaultUrl?: string;
}

const UrlInputField = ({ defaultUrl = '' }: UrlInputFieldProps) => {
  const [value, setValue] = useState(defaultUrl);

  return (
    <div className='flex w-full items-center gap-4'>
      <div className='flex flex-1 items-center gap-3 rounded border border-gray-200 px-3 py-2'>
        <Icons id='search' />
        <input
          value={value}
          onChange={(e) => {
            setValue(e.target.value);
          }}
          placeholder='학습할 영상의 링크를 입력하세요.'
          className='w-full text-lg focus:outline-none'
        />
      </div>

      <BasicButton isDisabled={value === ''} title='확인' onClick={() => {}} />
    </div>
  );
};

export default UrlInputField;
