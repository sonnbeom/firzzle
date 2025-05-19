'use client';

import { useEffect, useRef } from 'react';
import { getNewExamChat } from '@/api/learningChat';
import { useChatStore } from '@/stores/chatStore';
import { LearningChat, Mode } from 'types/learningChat';
import ChatBubble from './ChatBubble';

interface ChatHistoryProps {
  chats: LearningChat[];
  isLoading?: boolean;
  contentId: string;
  currentMode: Mode;
}

const ChatHistory = ({
  chats,
  isLoading,
  contentId,
  currentMode,
}: ChatHistoryProps) => {
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const { setCurrentExamSeq } = useChatStore();

  // 시험모드 처음 조회 시 새질문 생성
  useEffect(() => {
    const postNewExamChat = async () => {
      const response = await getNewExamChat(contentId);

      setCurrentExamSeq(response.exam_seq);
    };

    postNewExamChat();
  }, []);

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
          if (currentMode === '학습모드') {
            return a.indate.localeCompare(b.indate);
          }
          return 0;
        })
        .slice()
        .reduce((acc, chat) => {
          // 시험모드일 때는 배열을 뒤집음
          if (currentMode === '시험모드') {
            return [chat, ...acc];
          }
          return [...acc, chat];
        }, [] as LearningChat[])
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
