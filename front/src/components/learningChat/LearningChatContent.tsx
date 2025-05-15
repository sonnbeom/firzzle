'use client';

import { useState } from 'react';
import { Mode } from 'types/learningChat';
import ChatHistory from './ChatHistory';
import ChatTextAreaField from './ChatTextAreaField';
import ModeSelector from './ModeSelector';
import NewChatButton from './NewChatButton';

interface LearningChatContentProps {
  contentId: string;
}

const LearningChatContent = ({ contentId }: LearningChatContentProps) => {
  const [currentMode, setCurrentMode] = useState<Mode>('학습모드');

  return (
    <div className='flex h-full flex-col gap-2 overflow-hidden rounded-lg bg-gray-50 px-4 py-3'>
      {/* 채팅 선택 필드 */}
      <div className='flex w-full items-center justify-between'>
        <ModeSelector
          currentMode={currentMode}
          setCurrentMode={setCurrentMode}
        />
        {currentMode === '시험모드' && <NewChatButton />}
      </div>
      w{/* 채팅 내역 필드 */}
      <div className='flex-1 overflow-y-auto'>
        <ChatHistory currentMode={currentMode} />
      </div>
      {/* 채팅 입력 필드 */}
      <ChatTextAreaField mode={currentMode} contentId={contentId} />
    </div>
  );
};

export default LearningChatContent;
