interface ChatBubbleProps {
  text: string;
}

const ChatBubble = ({ text }: ChatBubbleProps) => {
  return (
    <div className='max-w-[80%] rounded-tl-lg rounded-b-lg bg-blue-300 px-4 py-1'>
      <p className='break-words whitespace-pre-wrap text-white'>{text}</p>
    </div>
  );
};

export default ChatBubble;
