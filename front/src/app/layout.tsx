import { ReactNode } from 'react';
import BasicToaster from '@/components/common/BasicToaster';
import Header from '@/components/common/Header';
import './global.css';
import { QueryProvider } from '@/utils/queryProvider';
import type { Metadata } from 'next';

export const viewport = {
  themeColor: '#ffffff',
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: 'cover',
};

export const metadata: Metadata = {
  title: 'Firzzle',
  description: 'AI 영상 분석으로 빠르고 간편하게 학습하세요!',
  manifest: '/manifest.json',
  themeColor: '#ffffff',
  viewport:
    'minimum-scale=1, initial-scale=1, width=device-width, shrink-to-fit=no, viewport-fit=cover',
  openGraph: {
    title: 'Firzzle',
    description: 'AI 영상 분석으로 빠르고 간편하게 학습하세요!',
    url: 'https://firzzle.site',
    siteName: 'Firzzle',
    images: [
      {
        url: 'https://firzzle.site/logo-512.png',
        width: 512,
        height: 512,
        alt: 'Firzzle Logo',
      },
    ],
    locale: 'ko_KR',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Firzzle',
    description: 'AI 영상 분석으로 빠르고 간편하게 학습하세요!',
    images: ['https://firzzle.site/logo-512.png'],
  },
  appleWebApp: {
    capable: true,
    statusBarStyle: 'default',
    title: 'Firzzle',
  },
  formatDetection: {
    telephone: false,
  },
  icons: {
    icon: [
      {
        url: 'https://firzzle.site/logo-192.png',
        sizes: '192x192',
        type: 'image/png',
      },
      {
        url: 'https://firzzle.site/logo-512.png',
        sizes: '512x512',
        type: 'image/png',
      },
    ],
    apple: [
      {
        url: 'https://firzzle.site/logo-192.png',
        sizes: '180x180',
        type: 'image/png',
      },
    ],
    shortcut: 'https://firzzle.site/logo-192.png',
  },
  other: {
    'apple-mobile-web-app-capable': 'yes',
    'apple-mobile-web-app-status-bar-style': 'default',
    'apple-mobile-web-app-title': 'Firzzle',
    'mobile-web-app-capable': 'yes',
    'application-name': 'Firzzle',
    'msapplication-TileColor': '#ffffff',
    'msapplication-TileImage': '/logo-192.png',
  },
};
<link rel='manifest' href='/manifest.json'></link>;

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang='en'>
      <head>
        <link rel='apple-touch-icon' href='/logo-192.png' />
        <meta name='apple-mobile-web-app-capable' content='yes' />
        <meta name='apple-mobile-web-app-status-bar-style' content='default' />
        <meta name='theme-color' content='#ffffff' />
      </head>
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
