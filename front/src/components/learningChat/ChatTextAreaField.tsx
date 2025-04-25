'use client';

import { useState } from 'react';
import Icons from '../common/Icons';

const ChatTextAreaField = () => {
  const [value, setValue] = useState('');

  return (
    <div className='flex items-end gap-1 rounded bg-white py-2 pr-4 pl-2'>
      <textarea
        value={value}
        onChange={(e) => {
          if (e.target.value !== '') {
            e.target.style.height = 'auto';
            e.target.style.height = e.target.scrollHeight + 'px';
          }
          setValue(e.target.value);
        }}
        placeholder='어떤 정보가 궁금하신가요?'
        className='w-full flex-1 resize-none rounded-md border border-none border-gray-300 px-2 text-gray-900 focus:outline-none'
        style={{ height: value ? 'auto' : '24px' }}
      />
      <button>
        <Icons id={value ? 'upload' : 'write'} />
      </button>
    </div>
  );
};

export default ChatTextAreaField;
