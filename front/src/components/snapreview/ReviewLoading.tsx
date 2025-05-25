import BasicSkeleton from '../common/BasicSkeleton';

const ReviewLoading = () => {
  return (
    <div className='flex h-full w-full flex-col gap-4'>
      <BasicSkeleton className='h-20 w-full' />
      <BasicSkeleton className='h-20 w-full' />
      <BasicSkeleton className='h-20 w-full' />
      <BasicSkeleton className='h-20 w-full' />
    </div>
  );
};

export default ReviewLoading;
