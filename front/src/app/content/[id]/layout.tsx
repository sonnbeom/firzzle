import SideMenu from '@/components/common/SideMenu';
import DetailHeader from '@/components/common/TabHeader';

const DetailLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className='flex h-full w-full gap-5'>
      <div className='flex flex-3'>
        <SideMenu />
      </div>
      <div className='flex flex-7 flex-col items-center gap-4'>
        <DetailHeader />
        {children}
      </div>
    </div>
  );
};

export default DetailLayout;
