import { ReactNode } from 'react';
import SideMenu from '@/components/common/SideMenu';

const MyLearningLayout = ({ children }: { children: ReactNode }) => {
  return (
    <div className='flex h-full w-full gap-8 overflow-y-auto px-6 py-8'>
      <div className='flex-[1.5]'>
        <SideMenu />
      </div>
      <div className='flex-[8.5]'>{children}</div>
    </div>
  );
};

export default MyLearningLayout;
