import BasicSkeleton from '../BasicSkeleton';

const ContentsLoading = () => {
  return (
    <div className='grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3'>
      <BasicSkeleton width={350} height={150} rounded={8} />
      <BasicSkeleton width={350} height={150} rounded={8} />
      <BasicSkeleton width={350} height={150} rounded={8} />
      <BasicSkeleton width={350} height={150} rounded={8} />
      <BasicSkeleton width={350} height={150} rounded={8} />
    </div>
  );
};

export default ContentsLoading;
