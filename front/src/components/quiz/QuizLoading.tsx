import BasicSkeleton from '../common/BasicSkeleton';

const QuizLoading = () => {
  return (
    <div className='h-full w-full space-y-6 pb-28'>
      <BasicSkeleton className='h-20 w-full rounded-[14px] px-8 py-2 md:py-4' />
      <BasicSkeleton className='h-20 w-full rounded-[14px] px-8 py-2 md:py-4' />
      <BasicSkeleton className='h-20 w-full rounded-[14px] px-8 py-2 md:py-4' />
    </div>
  );
};

export default QuizLoading;
