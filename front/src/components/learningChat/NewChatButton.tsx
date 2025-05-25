'use client';

import { getNewExamChat } from '@/api/learningChat';
import { useChatStore } from '@/stores/chatStore';
import { LearningChat } from '@/types/learningChat';
import { MAX_NEW_EXAM_COUNT } from '@/utils/const';
import BasicToaster from '../common/BasicToaster';
import Icons from '../common/Icons';

interface NewChatButtonProps {
  contentId: string;
  addOptimisticChat?: (chat: LearningChat) => void;
}

const NewChatButton = ({
  contentId,
  addOptimisticChat,
}: NewChatButtonProps) => {
  const { setCurrentExamSeq, solvedCount } = useChatStore();

  const handleNewChat = async () => {
    if (solvedCount >= MAX_NEW_EXAM_COUNT) {
      BasicToaster.default('질문 횟수 초과로 시험모드가 종료되었습니다.', {
        id: 'new-chat',
        duration: 2000,
      });

      return;
    }
    try {
      const response = await getNewExamChat(contentId);

      setCurrentExamSeq(response.exam_seq);

      const currentTime = new Date().toISOString();
      const optimisticChat: LearningChat = {
        chatSeq: Date.now().toString(),
        chatText: response.question,
        indate: currentTime,
        type: '1', // 시스템 메시지
      };

      // 낙관적 업데이트 적용
      if (addOptimisticChat) {
        addOptimisticChat(optimisticChat);
      }
    } catch (error) {
      BasicToaster.error(error.message);
    }
  };

  return (
    <div className='flex items-center gap-2'>
      <button
        className='flex cursor-pointer items-start gap-1'
        aria-label='새 질문 생성'
        onClick={handleNewChat}
      >
        <Icons id='new-chat' size={22} />
        <p className='text-gray-950'>새 질문 생성</p>
      </button>
      <p className='text-center text-sm text-gray-700'>
        {solvedCount}/{MAX_NEW_EXAM_COUNT}
      </p>
    </div>
  );
};

export default NewChatButton;
