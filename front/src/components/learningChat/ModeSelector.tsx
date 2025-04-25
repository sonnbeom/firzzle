'use client';

import { useState } from 'react';
import Icons from '../common/Icons';
import { Button } from '../ui/button';

type Mode = '학습모드' | '시험모드';

const ModeSelector = () => {
  const [currentMode, setCurrentMode] = useState<Mode>('학습모드');
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className='flex w-fit flex-col gap-1'>
      {/* 현재 모드 */}
      <div className='flex w-fit items-center gap-2 rounded-sm border border-gray-300 bg-white py-1 pr-3 pl-2'>
        <p>{currentMode}</p>
        <button
          aria-label={isOpen ? '모드 선택 목록 닫기' : '모드 선택 목록 열기'}
          onClick={() => setIsOpen(!isOpen)}
        >
          <Icons id={isOpen ? 'arrow-up' : 'arrow-down'} size={16} />
        </button>
      </div>

      {/* 모드 선택 */}
      {isOpen && (
        <div className='flex flex-col rounded-sm border border-gray-300 bg-white'>
          {(['학습모드', '시험모드'] as Mode[]).map((mode) => (
            <Button
              key={mode}
              variant='text'
              className={`text-md ${currentMode !== mode && 'font-regular text-gray-500'} px-0`}
              onClick={() => setCurrentMode(mode)}
            >
              <p>{mode}</p>
            </Button>
          ))}
        </div>
      )}
    </div>
  );
};

export default ModeSelector;
