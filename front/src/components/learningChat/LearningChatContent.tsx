'use client';

import { useState } from 'react';
import { getLearningChatHistory } from '@/api/learningChat';
import { useChatHistory } from '@/hooks/useChatHistory';
import { Mode, LearningChat } from '@/types/learningChat';
import ChatHistory from './ChatHistory';
import ChatTextAreaField from './ChatTextAreaField';
import ModeSelector from './ModeSelector';
import NewChatButton from './NewChatButton';

interface LearningChatContentProps {
  contentId: string;
}

const LearningChatContent = ({ contentId }: LearningChatContentProps) => {
  const [currentMode, setCurrentMode] = useState<Mode>('학습모드');
  const [isLoading, setIsLoading] = useState(false);

  const {
    data: chatHistory,
    hasNextPage,
    observerTarget,
    refetch,
    addOptimisticChat,
  } = useChatHistory<LearningChat>({
    queryKey: ['learningChatHistory', contentId],
    queryFn: (lastIndate) => getLearningChatHistory(contentId, lastIndate),
  });

  const handleLoadingChange = (loading: boolean) => {
    setIsLoading(loading);
  };

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
      {/* 채팅 내역 필드 */}
      <div className='flex-1 overflow-y-auto'>
        <ChatHistory
          currentMode={currentMode}
          contentId={contentId}
          chats={chatHistory || []}
          refetch={refetch}
          isLoading={isLoading}
        />
      </div>
      {/* 채팅 입력 필드 */}
      <ChatTextAreaField
        mode={currentMode}
        contentId={contentId}
        refetch={refetch}
        addOptimisticChat={addOptimisticChat}
        onLoadingChange={handleLoadingChange}
      />
    </div>
  );
};

export default LearningChatContent;
