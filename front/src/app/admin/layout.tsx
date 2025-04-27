import { ReactNode } from 'react';
import AdminSideMenu from '@/components/admin/AdminSideMenu';

const AdminLayout = ({ children }: { children: ReactNode }) => {
  return (
    <div className='flex h-full w-full'>
      <div className='h-full flex-[1.5]'>
        <AdminSideMenu />
      </div>
      <div className='flex-[8.5]'>{children}</div>
    </div>
  );
};

export default AdminLayout;
