import BasicSkeleton from '@/components/common/BasicSkeleton';

const SnapBookLoading = () => {
  return (
    <div className='flex flex-col gap-4'>
      <BasicSkeleton className='h-[40px] w-[300px]' />
      <div className='grid grid-cols-3 gap-4 md:grid-cols-4'>
        <BasicSkeleton className='h-[220px] max-w-[240px] lg:h-[260px]' />
        <BasicSkeleton className='h-[220px] max-w-[240px] lg:h-[260px]' />
        <BasicSkeleton className='h-[220px] max-w-[240px] lg:h-[260px]' />
        <BasicSkeleton className='h-[220px] max-w-[240px] lg:h-[260px]' />
      </div>
    </div>
  );
};

export default SnapBookLoading;
