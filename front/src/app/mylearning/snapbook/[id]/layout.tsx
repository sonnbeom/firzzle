'use client';

import { ReactNode } from 'react';
import { Toaster } from 'sonner';

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <>
      {children}
      <Toaster
        position='bottom-center'
        duration={1500}
        theme='dark'
        toastOptions={{
          style: {
            background: 'rgba(26, 26, 26, 0.8)',
            color: '#fff',
          },
          className: 'text-sm font-medium',
        }}
      />
    </>
  );
}
