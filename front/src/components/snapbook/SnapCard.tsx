import Image from 'next/image';
import React from 'react';

interface SnapItem {
  id: string;
  title: string;
  thumbnail: string;
  date: string;
  length: number;
}

interface SnapCardProps {
  data: SnapItem;
}

const SnapCard = ({ data }: SnapCardProps) => {
  return (
    <div className='group mx-auto max-w-[240px] overflow-hidden rounded-lg bg-white shadow-md transition-all'>
      <div className='relative aspect-video'>
        <Image
          src={data.thumbnail}
          alt={data.title}
          fill
          className='object-cover transition-transform duration-300 group-hover:scale-105'
        />
      </div>
      <div className='p-4'>
        <span className='text-md font-semibold text-gray-950 md:text-xl'>
          스냅 {data.length}컷
        </span>
        <h3 className='mb-2 line-clamp-2 text-lg'>{data.title}</h3>
      </div>
    </div>
  );
};

export default SnapCard;
