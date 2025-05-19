import BasicSkeleton from '../BasicSkeleton';

const ContentsLoading = () => {
  return (
    <div className='grid w-full grid-cols-1 gap-4 md:grid-cols-2 xl:gap-8 2xl:grid-cols-3'>
      <BasicSkeleton className='w-full' />
      <BasicSkeleton className='w-full' />
      <BasicSkeleton className='w-full' />
      <BasicSkeleton className='w-full' />
      <BasicSkeleton className='w-full' />
    </div>
  );
};

export default ContentsLoading;
