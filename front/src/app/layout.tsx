import { ReactNode } from 'react';
import BasicToaster from '@/components/common/BasicToaster';
import Header from '@/components/common/Header';
import './global.css';
import { QueryProvider } from '@/utils/queryProvider';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body>
        <QueryProvider>
          <div className='!important relative flex h-[100dvh] w-full min-w-[320px] flex-col'>
            <Header />
            <BasicToaster />
            <div className='flex-1'>{children}</div>
          </div>
        </QueryProvider>
      </body>
    </html>
  );
}
