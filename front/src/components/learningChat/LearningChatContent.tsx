import ChatHistory from './ChatHistory';
import ChatTextAreaField from './ChatTextAreaField';
import ModeSelector from './ModeSelector';
import NewChatButton from './NewChatButton';

const LearningChatContent = () => {
  return (
    <div className='flex h-full flex-col gap-2 overflow-hidden rounded-lg bg-gray-50 px-4 py-3'>
      {/* 채팅 선택 필드 */}
      <div className='flex w-full items-center justify-between'>
        <ModeSelector />
        <NewChatButton />
      </div>
      {/* 채팅 내역 필드 */}
      <div className='flex-1 overflow-y-auto'>
        <ChatHistory />
      </div>
      {/* 채팅 입력 필드 */}
      <ChatTextAreaField />
    </div>
  );
};

export default LearningChatContent;
