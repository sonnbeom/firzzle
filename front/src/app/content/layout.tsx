import SideMenuButton from '@/components/common/SideMenuButton';

const ContentLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className='flex h-full w-full flex-col gap-3 overflow-y-auto px-6 py-5'>
      <SideMenuButton />
      {children}
    </div>
  );
};

export default ContentLayout;
