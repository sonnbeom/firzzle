'use client';

import { useEffect, useRef } from 'react';
import { getLearningChatHistory } from '@/api/learningChat';
import { useChatHistory } from '@/hooks/useChatHistory';
import { LearningChat, Mode } from 'types/learningChat';
import ChatBubble from './ChatBubble';

interface ChatHistoryProps {
  currentMode: Mode;
  contentId: string;
}

const ChatHistory = ({ currentMode, contentId }: ChatHistoryProps) => {
  const chatHistoryRef = useRef<HTMLDivElement>(null);

  const {
    data: chatHistory,

    hasNextPage,
    observerTarget,
  } = useChatHistory<LearningChat>({
    queryKey: ['learningChatHistory', contentId],
    queryFn: (lastIndate) => getLearningChatHistory(contentId, lastIndate),
  });

  // 처음 렌더링시 또는 새 메시지가 추가될 때 스크롤을 맨 아래로 이동
  useEffect(() => {
    if (chatHistoryRef.current) {
      // 첫번째 로드 시에만 스크롤을 맨 아래로 이동
      if (chatHistory.length > 0 && chatHistory.length <= 10) {
        chatHistoryRef.current.scrollTop = chatHistoryRef.current.scrollHeight;
      }
    }
  }, [chatHistory]);

  return (
    <div
      ref={chatHistoryRef}
      className='flex h-full w-full flex-col gap-4 overflow-y-auto'
    >
      {hasNextPage && <div ref={observerTarget} className='mt-4 h-1' />}

      {chatHistory.map((chat, index) => (
        <ChatBubble key={index} chat={chat} />
      ))}
    </div>
  );
};

export default ChatHistory;
