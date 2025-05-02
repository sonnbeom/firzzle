'use client';

import { usePathname } from 'next/navigation';
import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import Share from './Share';

export default function ShareButton() {
  const [isShareOpen, setIsShareOpen] = useState(false);
  const [shareUrl, setShareUrl] = useState('');
  const pathname = usePathname();
  const id = pathname.split('/').pop();

  useEffect(() => {
    const origin = window.location.origin;
    setShareUrl(
      `${process.env.NEXT_PUBLIC_BASE_URL || origin}/share/snapbook/${id}`,
    );
  }, [id]);

  return (
    <>
      <Button
        variant='default'
        className='text-xs sm:text-lg'
        onClick={() => setIsShareOpen(true)}
      >
        공유하기
      </Button>
      <Share
        isOpen={isShareOpen}
        onClose={() => setIsShareOpen(false)}
        url={shareUrl}
      />
    </>
  );
}
