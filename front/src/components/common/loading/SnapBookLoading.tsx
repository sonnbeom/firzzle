import BasicSkeleton from '@/components/common/BasicSkeleton';

const SnapBookLoading = () => {
  return (
    <div className='flex flex-col gap-4'>
      <BasicSkeleton width={300} height={40} rounded={8} />
      <div className='grid grid-cols-1 gap-4 md:grid-cols-3 lg:grid-cols-4'>
        <BasicSkeleton width={240} height={260} rounded={8} />
        <BasicSkeleton width={240} height={260} rounded={8} />
        <BasicSkeleton width={240} height={260} rounded={8} />
        <BasicSkeleton width={240} height={260} rounded={8} />
      </div>
    </div>
  );
};

export default SnapBookLoading;
