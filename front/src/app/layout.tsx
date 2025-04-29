import { ReactNode } from 'react';
import Header from '@/components/common/Header';
import { QueryProvider } from 'providers/QueryProvider';
import './global.css';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body>
        <QueryProvider>
          <div className='relative flex h-[100dvh] w-full flex-col'>
            <Header />
            <div className='flex-1 overflow-hidden'>{children}</div>
          </div>
        </QueryProvider>
      </body>
    </html>
  );
}
