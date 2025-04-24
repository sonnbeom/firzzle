import DetailHeader from '@/components/common/TabHeader';

const DetailLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className='flex w-full'>
      <div className='flex-3'>유튜브, 러닝챗</div>
      <div className='flex flex-7 flex-col items-center gap-4 px-5'>
        <DetailHeader />
        {children}
      </div>
    </div>
  );
};

export default DetailLayout;
