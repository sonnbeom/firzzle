'use client';

import Image from 'next/image';
import { Expert } from '@/types/recommend';

interface ExpertCardProps {
  expert: Expert;
}

const ExpertCard = ({ expert }: ExpertCardProps) => (
  <div className='flex h-[240px] w-full flex-col items-center rounded-lg bg-white p-2 shadow-md sm:h-[280px] sm:rounded-xl sm:p-4'>
    <div className='relative mb-2 h-12 w-12 overflow-hidden rounded-full border-2 border-blue-200 sm:h-16 sm:w-16 lg:h-20 lg:w-20'>
      <Image
        src={expert.profileImageUrl}
        alt={expert.name}
        fill
        sizes='(max-width: 768px) 64px, 80px'
        className='object-cover'
      />
    </div>
    <div className='mb-1 text-sm font-bold sm:text-base lg:text-lg'>{expert.name}</div>
    <div className='mb-1 text-center text-[10px] text-gray-700 sm:mb-2 sm:text-xs lg:text-sm'>
      {expert.company}
    </div>
    <div className='mb-1 text-center text-[10px] text-gray-700 sm:mb-2 sm:text-xs lg:text-sm'>
      {expert.title}
    </div>
    <div className='mb-2 flex flex-wrap justify-center gap-1'>
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

export default ExpertCard;
