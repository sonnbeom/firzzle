import { ReactNode } from 'react';
import Header from '@/components/common/Header';
import { QueryProvider } from 'layouts/QueryProvider';
import './global.css';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body className='scrollbar-none'>
        <QueryProvider>
          <div className='flex h-screen w-full flex-col'>
            <Header />
            {children}
          </div>
        </QueryProvider>
      </body>
    </html>
  );
}
