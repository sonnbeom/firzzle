import { LearningChat } from '@/types/learningChat';

interface ChatBubbleProps {
  chat: LearningChat;
}

const ChatBubble = ({ chat }: ChatBubbleProps) => {
  return chat.type === '0' ? (
    <div className='w-full font-medium whitespace-pre-wrap text-gray-950'>
      {chat.chatText}
    </div>
  ) : (
    <div className='flex w-full justify-end'>
      <div className='max-w-[80%] rounded-tl-lg rounded-b-lg bg-blue-300 px-4 py-1'>
        <p className='break-words whitespace-pre-wrap text-white'>
          {chat.chatText}
        </p>
      </div>
    </div>
  );
};

export default ChatBubble;
