import { ReactNode } from 'react';
import { QueryProvider } from 'providers/QueryProvider';
import './global.css';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body>
        <QueryProvider>{children}</QueryProvider>
      </body>
    </html>
  );
}
