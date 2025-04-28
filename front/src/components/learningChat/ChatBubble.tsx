import { LEARNING_CHATBOT_ID } from 'utils/const';

interface ChatBubbleProps {
  userId: string;
  text: string;
}

const ChatBubble = ({ userId, text }: ChatBubbleProps) => {
  return userId === LEARNING_CHATBOT_ID ? (
    <div className='w-full font-medium whitespace-pre-wrap text-gray-950'>
      {text}
    </div>
  ) : (
    <div className='flex w-full justify-end'>
      <div className='max-w-[80%] rounded-tl-lg rounded-b-lg bg-blue-300 px-4 py-1'>
        <p className='break-words whitespace-pre-wrap text-white'>{text}</p>
      </div>
    </div>
  );
};

export default ChatBubble;
