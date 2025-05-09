'use client';

import Image from 'next/image';
import { Expert } from '@/types/recommend';

interface ExpertCardProps {
  expert: Expert;
}

const ExpertCard = ({ expert }: ExpertCardProps) => (
  <div className='flex h-[280px] w-full flex-col items-center rounded-xl bg-white p-4 shadow-md lg:w-56'>
    <div className='relative mb-2 h-16 w-16 overflow-hidden rounded-full border-2 border-blue-200 lg:h-20 lg:w-20'>
      <Image
        src={expert.profileImageUrl}
        alt={expert.name}
        fill
        sizes='(max-width: 768px) 64px, 80px'
        className='object-cover'
      />
    </div>
    <div className='mb-1 text-base font-bold lg:text-lg'>{expert.name}</div>
    <div className='mb-2 text-center text-xs text-gray-700 lg:text-sm'>
      {expert.company}
    </div>
    <div className='mb-2 text-center text-xs text-gray-700 lg:text-sm'>
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
        className='rounded-full bg-blue-500 px-4 py-1 text-sm text-white hover:bg-blue-600'
      >
        LinkedIn 프로필
      </a>
    </div>
  </div>
);

export default ExpertCard;
