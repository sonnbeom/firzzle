'use client';

import { throttle } from 'lodash';
import { useCallback, useState } from 'react';
import Icons from '../common/Icons';

const NewChatButton = () => {
  const dummyData = {
    currentCount: 1,
    total: 10,
  };

  const [currentCount, setCurrentCount] = useState(dummyData.currentCount);

  const handleClick = useCallback(
    throttle(
      () => {
        setCurrentCount((prevCount) => {
          console.log(prevCount);
          return prevCount + 1;
        });
      },
      1000,
      { trailing: false },
    ),
    [],
  );

  return (
    <div className='flex flex-col items-end'>
      {currentCount < dummyData.total && (
        <button
          className='flex cursor-pointer items-center gap-2'
          aria-label='새 질문 생성'
          onClick={handleClick}
          disabled={currentCount === dummyData.total}
        >
          <Icons id='new-chat' size={20} />
          <p className='text-gray-950'>새 질문 생성</p>
        </button>
      )}
      <p className='text-sm text-gray-700'>
        {currentCount < dummyData.total
          ? `${currentCount} / ${dummyData.total}`
          : '문제 풀이 완료'}
      </p>
    </div>
  );
};

export default NewChatButton;
