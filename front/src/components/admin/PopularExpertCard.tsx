import Image from 'next/image';
import React from 'react';

interface PopularExpertCardProps {
  imageUrl?: string;
  name: string;
  description: string;
  tags?: string[];
}

const PopularExpertCard = ({
  imageUrl = '/assets/images/Firzzle.png',
  name,
  description,
  tags = [],
}: PopularExpertCardProps) => (
  <div className='flex w-56 flex-col items-center rounded-xl bg-white p-4 shadow-md'>
    <div className='relative mb-2 h-20 w-20 overflow-hidden rounded-full border-2 border-blue-200'>
      <Image src={imageUrl} alt={name} fill className='object-cover' />
    </div>
    <div className='mb-1 text-lg font-bold'>{name}</div>
    <div className='mb-2 text-center text-xs text-gray-500'>{description}</div>
    <div className='mb-2 flex flex-wrap justify-center gap-1'>
      {tags.map((tag) => (
        <span
          key={tag}
          className='rounded-full bg-blue-50 px-2 py-0.5 text-xs text-blue-400'
        >
          {tag}
        </span>
      ))}
    </div>
    <div className='flex gap-3 text-xs text-gray-400'>
      <span className='flex items-center gap-1'>
        <i className='fa fa-users' /> 1,234
      </span>
      <span className='flex items-center gap-1'>
        <i className='fa fa-star' /> 4.9
      </span>
    </div>
  </div>
);

export default PopularExpertCard;
