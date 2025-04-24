import { ReactNode } from 'react';
import Header from '@/components/common/Header';
import { QueryProvider } from 'layouts/QueryProvider';
import './global.css';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body className='scrollbar-none'>
        <QueryProvider>
          <div className='flex min-h-screen w-full flex-col overflow-y-auto'>
            <Header />
            {children}
          </div>
        </QueryProvider>
      </body>
    </html>
  );
}
