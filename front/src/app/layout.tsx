import { ReactNode } from 'react';
import Header from '@/components/common/Header';
import './global.css';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body>
        <div className='relative flex h-[100dvh] w-full flex-col'>
          <Header />
          <div className='flex-1'>{children}</div>
        </div>
      </body>
    </html>
  );
}
