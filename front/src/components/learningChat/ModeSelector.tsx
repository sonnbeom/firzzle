'use client';

import { useState } from 'react';
import { Mode } from 'types/learningChat';
import Icons from '../common/Icons';
import { Button } from '../ui/button';

interface ModeSelectorProps {
  currentMode: Mode;
  setCurrentMode: (mode: Mode) => void;
}

const ModeSelector = ({ currentMode, setCurrentMode }: ModeSelectorProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleModeChange = (mode: Mode) => {
    setCurrentMode(mode);
    setIsOpen(false);
  };

  return (
    <div className='relative flex w-fit flex-col gap-1'>
      {/* 현재 모드 */}
      <button
        aria-label={isOpen ? '모드 선택 목록 닫기' : '모드 선택 목록 열기'}
        onClick={() => setIsOpen(!isOpen)}
        className='flex w-fit cursor-pointer items-center gap-2 rounded-sm border border-gray-300 bg-white py-1 pr-3 pl-2'
      >
        <p>{currentMode}</p>
        <Icons id={isOpen ? 'arrow-up' : 'arrow-down'} size={16} />
      </button>

      {/* 모드 선택 */}
      {isOpen && (
        <div className='absolute top-full left-0 z-50 flex w-full translate-y-1 flex-col rounded-sm border border-gray-300 bg-white opacity-100 transition-all duration-200'>
          {(['학습모드', '시험모드'] as Mode[]).map((mode) => (
            <Button
              key={mode}
              variant='text'
              className={`text-md ${currentMode !== mode && 'font-regular text-gray-500'} px-0`}
              onClick={() => handleModeChange(mode)}
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
