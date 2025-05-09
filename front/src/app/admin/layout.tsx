import { ReactNode } from 'react';
import AdminSideMenu from '@/components/admin/AdminSideMenu';

const AdminLayout = ({ children }: { children: ReactNode }) => {
  return (
    <div className='flex h-full w-full flex-col'>
      <div className='lg:hidden'>
        <AdminSideMenu />
      </div>
      <div className='flex h-full w-full flex-row'>
        <div className='hidden lg:block lg:h-full lg:w-64 lg:min-w-[16rem]'>
          <AdminSideMenu />
        </div>
        <div className='flex-1 overflow-auto'>{children}</div>
      </div>
    </div>
  );
};

export default AdminLayout;
