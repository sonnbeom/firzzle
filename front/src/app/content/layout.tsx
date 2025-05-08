import { ReactNode } from 'react';

const ContentLayout = ({ children }: { children: ReactNode }) => {
  return (
    <div className='flex h-full w-full flex-col gap-3 overflow-y-auto px-4 py-3 lg:px-6 lg:py-5'>
      {children}
    </div>
  );
};

export default ContentLayout;
