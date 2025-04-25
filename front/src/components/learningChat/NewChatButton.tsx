import Icons from '../common/Icons';

const NewChatButton = () => {
  return (
    <button className='flex items-center gap-2' aria-label='새 질문 생성'>
      <Icons id='newchat' size={20} />
      <p className='text-gray-950'>새 질문 생성</p>
    </button>
  );
};

export default NewChatButton;
