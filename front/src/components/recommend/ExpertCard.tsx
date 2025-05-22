'use client';

import Image from 'next/image';
import { Expert } from '@/types/recommend';

interface ExpertCardProps {
  expert: Expert;
}

const ExpertCard = ({ expert }: ExpertCardProps) => {
  const imageUrl =
    expert.profileImageUrl && expert.profileImageUrl.trim() !== ''
      ? expert.profileImageUrl
      : '/assets/images/Firzzle.png';

  return (
    <div className='flex min-h-[300px] w-full flex-col items-center rounded-lg bg-white p-2 shadow-md'>
      <div className='relative mb-2 aspect-square w-24 shrink-0 overflow-hidden rounded-full border-2 border-blue-400 bg-white'>
        <Image
          src={imageUrl}
          alt={expert.name}
          fill
          sizes='96px'
          className='object-cover'
        />
      </div>

      <div className='mb-1 text-center text-sm font-bold sm:text-base lg:text-lg'>
        {expert.name}
      </div>
      <div className='mb-1 text-center text-[10px] text-gray-700 sm:mb-2 sm:text-xs lg:text-sm'>
        {expert.company}
      </div>
      <div className='mb-1 text-center text-[10px] text-gray-700 sm:mb-2 sm:text-xs lg:text-sm'>
        {expert.title}
      </div>

      <div className='mb-2 flex flex-wrap justify-center gap-1 px-2'>
        {expert.expertise.slice(0, 3).map((tag) => (
          <span
            key={tag}
            className='rounded-full bg-blue-50 px-2 py-0.5 text-xs text-blue-400'
          >
            {tag}
          </span>
        ))}
      </div>

      <div className='mt-auto'>
        <a
          href={expert.linkedinUrl}
          target='_blank'
          rel='noopener noreferrer'
          className='rounded-full bg-blue-500 px-4 py-1 text-xs text-white hover:bg-blue-600 sm:text-sm'
        >
          프로필 링크
        </a>
      </div>
    </div>
  );
};

export default ExpertCard;
