'use client';

import { useEffect, useRef } from 'react';
import { LearningChat, Mode } from 'types/learningChat';
import ChatBubble from './ChatBubble';

interface ChatHistoryProps {
  currentMode: Mode;
  contentId: string;
  chats: LearningChat[];
  refetch: () => void;
  isLoading?: boolean;
}

const ChatHistory = ({
  currentMode,
  contentId,
  chats,
  refetch,
  isLoading,
}: ChatHistoryProps) => {
  const chatContainerRef = useRef<HTMLDivElement>(null);

  // 스크롤을 항상 최하단으로 이동
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop =
        chatContainerRef.current.scrollHeight;
    }
  }, [chats, isLoading]);

  return (
    <div
      ref={chatContainerRef}
      className='flex h-full flex-col gap-4 overflow-y-auto px-2 py-4'
    >
      {[...chats]
        .sort((a, b) => {
          // chatSeq를 숫자로 변환하여 내림차순 정렬
          const seqA = parseInt(a.chatSeq);
          const seqB = parseInt(b.chatSeq);
          return seqA - seqB;
        })
        .map((chat, index) => (
          <ChatBubble key={index} chat={chat} />
        ))}
      {isLoading && (
        <div className='w-full font-medium whitespace-pre-wrap text-gray-950'>
          응답 생성 중...
        </div>
      )}
    </div>
  );
};

export default ChatHistory;
