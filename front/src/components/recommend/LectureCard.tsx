'use client';

import Image from 'next/image';
import Link from 'next/link';
import { memo } from 'react';
import { VideoProps } from '@/types/recommend';

interface LectureCardProps {
  video: VideoProps;
}

const LectureCard = memo(({ video }: LectureCardProps) => {
  return (
    <Link
      href={`/content?url=${encodeURIComponent(video.url)}`}
      className='block cursor-pointer hover:opacity-80'
    >
      <div className='relative aspect-video w-full max-w-[300px] overflow-hidden rounded-lg border border-gray-200'>
        <Image
          src={video.thumbnailUrl}
          alt='강의 썸네일'
          width={300}
          height={169}
          style={{ objectFit: 'fill' }}
          priority={true}
          className='h-full w-full object-contain'
        />
      </div>
      <p className='mt-2 line-clamp-2 text-sm text-gray-700 px-2'>{video.title}</p>
    </Link>
  );
});

LectureCard.displayName = 'LectureCard';

export default LectureCard;
