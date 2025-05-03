'use client';

import { ChangeEvent, KeyboardEvent, useState } from 'react';
import { postExamChat, postLearningChat } from '@/api/learningChat';
import { Mode } from '@/types/learningChat';
import { MAX_LEARNING_CHAT_LENGTH } from 'utils/const';
import Icons from '../common/Icons';

interface ChatTextAreaFieldProps {
  mode: Mode;
  contentId: string;
}

const ChatTextAreaField = ({ mode, contentId }: ChatTextAreaFieldProps) => {
  const [value, setValue] = useState('');

  const onChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    // 글자수 제한
    if (e.target.value.trim().length > MAX_LEARNING_CHAT_LENGTH) {
      alert('허용 가능한 글자수를 초과하였습니다.');
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
    try {
      if (mode === '학습모드') {
        await postLearningChat();
      } else {
        await postExamChat(contentId);
      }
    } catch (error) {
      console.error('채팅 전송 실패:', error);
    } finally {
      setValue('');
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
        placeholder='어떤 정보가 궁금하신가요?'
        className='w-full flex-1 resize-none rounded-md border border-none border-gray-300 px-2 text-gray-900 focus:outline-none'
        style={{ height: value ? 'auto' : '24px' }}
      />
      <button onClick={handleSubmit}>
        <Icons id={value ? 'upload' : 'write'} />
      </button>
    </div>
  );
};

export default ChatTextAreaField;
