import BasicSkeleton from '../common/BasicSkeleton';

const ChatHistoryLoading = () => {
  return (
    <div className='flex h-full w-full flex-col justify-between px-4 py-3'>
      <div className='flex w-full flex-col gap-2'>
        {/* 모드 선택 필드 */}
        <BasicSkeleton className='h-10 w-20 rounded-sm' />

        {/* 채팅 내역 필드 */}
        <div className='flex w-full flex-col gap-4 py-4'>
          <BasicSkeleton className='ml-auto h-10 w-full max-w-[80%] rounded-sm' />
          <BasicSkeleton className='h-10 max-w-[80%] rounded-sm' />
          <BasicSkeleton className='ml-auto h-10 w-full max-w-[80%] rounded-sm' />
        </div>
      </div>

      {/* 채팅 입력 필드 */}
      <BasicSkeleton className='h-10 w-full rounded-sm' />
    </div>
  );
};

export default ChatHistoryLoading;
