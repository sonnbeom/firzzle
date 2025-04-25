import ChatBubble from './ChatBubble';
import ChatTextAreaField from './ChatTextAreaField';
import ModeSelector from './ModeSelector';
import NewChatButton from './NewChatButton';

const LearningChatContent = () => {
  return (
    <div className='flex h-full flex-col gap-5 rounded-lg bg-gray-50 px-4 py-3'>
      {/* 채팅 선택 필드 */}
      <div className='flex w-full items-center justify-between'>
        <ModeSelector />
        <NewChatButton />
      </div>
      {/* 채팅 내역 필드 */}
      <div className='w-full flex-1'>
        <div className='flex justify-end'>
          <ChatBubble text='안녕하세요' />
        </div>
      </div>
      {/* 채팅 입력 필드 */}
      <ChatTextAreaField />
    </div>
  );
};

export default LearningChatContent;
