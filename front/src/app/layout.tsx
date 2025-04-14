import { ReactNode } from 'react';
import { QueryProvider } from 'layouts/QueryProvider';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <body>
        <QueryProvider>{children}</QueryProvider>
      </body>
    </html>
  );
}
