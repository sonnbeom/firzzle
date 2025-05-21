'use client';

import { ChangeEvent, KeyboardEvent, useState } from 'react';
import { postExamChat, postLearningChat } from '@/api/learningChat';
import { useChatStore } from '@/stores/chatStore';
import { LearningChat, Mode } from '@/types/learningChat';
import { MAX_LEARNING_CHAT_LENGTH, MAX_NEW_EXAM_COUNT } from 'utils/const';
import BasicToaster from '../common/BasicToaster';
import Icons from '../common/Icons';

interface ChatTextAreaFieldProps {
  mode: Mode;
  contentId: string;
  refetch: () => void;
  addOptimisticChat?: (chat: LearningChat) => void;
  onLoadingChange?: (loading: boolean) => void;
}

const ChatTextAreaField = ({
  mode,
  contentId,
  refetch,
  addOptimisticChat,
  onLoadingChange,
}: ChatTextAreaFieldProps) => {
  const [value, setValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { currentExamSeq, solvedCount, setCurrentExamSeq, setSolvedCount } =
    useChatStore();

  const onChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    // 글자수 제한
    if (e.target.value.trim().length > MAX_LEARNING_CHAT_LENGTH) {
      BasicToaster.default('허용 가능한 글자수를 초과하였습니다.');
      return;
    }

    // 입력창 높이 조절
    if (e.target.value !== '') {
      e.target.style.height = 'auto';
      e.target.style.height = e.target.scrollHeight + 'px';
    }
    setValue(e.target.value);
  };

  // 채팅 입력
  const handleSubmit = async () => {
    if (value.trim() === '') {
      BasicToaster.default('메시지를 입력해주세요.');
      return;
    }

    setValue('');

    const optimisticChat: LearningChat = {
      chatSeq: 0,
      chatText: value,
      indate: new Date(Date.now() - 1000).toISOString(),
      type: '0', // 사용자 메시지
    };

    // 낙관적 업데이트 적용
    if (addOptimisticChat) {
      addOptimisticChat(optimisticChat);

      if (mode === '시험모드') {
        setCurrentExamSeq(currentExamSeq);
        setSolvedCount(solvedCount + 1);
      }
    }

    // 로딩 상태 시작
    setIsLoading(true);
    onLoadingChange?.(true);

    try {
      if (mode === '학습모드') {
        await postLearningChat(contentId, value);
      } else {
        await postExamChat(contentId, {
          exam_seq: currentExamSeq,
          answer: value,
        });
      }

      // 채팅 내역 갱신
      refetch();
    } catch (error) {
      BasicToaster.error(error.message);
      // 에러 발생 시 낙관적 업데이트 롤백
      refetch();
    } finally {
      setIsLoading(false);
      onLoadingChange?.(false);
    }
  };

  const onKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <div className='flex items-end gap-1 rounded bg-white py-2 pr-4 pl-2'>
      <textarea
        value={value}
        onChange={onChange}
        onKeyDown={onKeyDown}
        placeholder={
          mode === '시험모드'
            ? solvedCount >= MAX_NEW_EXAM_COUNT
              ? '질문 횟수 초과로 시험모드가 종료되었습니다.'
              : '영상 내용을 바탕으로 질문에 답해보세요.'
            : '어떤 정보가 궁금하신가요?'
        }
        className='w-full flex-1 resize-none rounded-md border border-none border-gray-300 px-2 text-gray-900 focus:outline-none'
        style={{ height: value ? 'auto' : '24px' }}
        disabled={
          isLoading ||
          (mode === '시험모드' && solvedCount >= MAX_NEW_EXAM_COUNT)
        }
      />
      <button onClick={handleSubmit} disabled={isLoading}>
        <Icons id='write' color={value ? 'text-gray-950' : 'text-gray-500'} />
      </button>
    </div>
  );
};

export default ChatTextAreaField;
