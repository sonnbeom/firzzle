import { ReactNode } from 'react';
import SideMenuButton from '@/components/common/SideMenuButton';

const ContentLayout = ({ children }: { children: ReactNode }) => {
  return (
    <div className='flex h-full w-full flex-col gap-3 overflow-y-auto px-6 py-5'>
      <SideMenuButton />
      <div className='flex-1 overflow-hidden'>{children}</div>
    </div>
  );
};

export default ContentLayout;
