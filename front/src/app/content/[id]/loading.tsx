import BasicSkeleton from '@/components/common/BasicSkeleton';

const Loading = () => {
  return (
    <div className='flex h-full w-full gap-5'>
      <div className='hidden flex-2 flex-col gap-6 lg:flex xl:flex-3'>
        <BasicSkeleton className='aspect-video w-full' />
        <BasicSkeleton className='flex h-full flex-col gap-2' />
      </div>
      <div className='flex flex-3 flex-col items-center gap-2 lg:gap-4 xl:flex-7'>
        <BasicSkeleton className='h-full w-full' />
      </div>
    </div>
  );
};

export default Loading;
