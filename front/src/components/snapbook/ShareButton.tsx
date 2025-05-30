'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import Share from './Share';

interface ShareButtonProps {
  shareUrl?: string;
}

export default function ShareButton({ shareUrl }: ShareButtonProps) {
  const [isShareOpen, setIsShareOpen] = useState(false);


  return (
    <>
      <Button
        variant='default'
        className='text-sm sm:text-md md:text-lg cursor-pointer'
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
