'use client';

import Image from 'next/image';
import React from 'react';

interface SnapItem {
  title: string;
  thumbnail: string;
  date: string;
}

interface DateGroup {
  date: string;
  items: SnapItem[];
}

interface SnapCardProps {
  data: DateGroup[];
}

const SnapCard = ({ data }: SnapCardProps) => {
  // Add error handling
  if (!data || !Array.isArray(data)) {
    console.error('SnapCard: Invalid data format:', data);
    return <div>Error: Invalid data format</div>;
  }

  // Debug log
  console.log('SnapCard: Rendering with data:', data);
  const handleScroll = (
    container: HTMLDivElement,
    direction: 'left' | 'right',
  ) => {
    const scrollAmount =
      direction === 'left' ? -container.clientWidth : container.clientWidth;
    container.scrollBy({ left: scrollAmount, behavior: 'smooth' });
  };

  return (
    <div className='space-y-8'>
      {data.map((group) => (
        <div key={group.date} className='space-y-4'>
          <h2 className='text-md font-medium text-gray-950 sm:text-xl'>
            {group.date}
          </h2>
          <div className='relative'>
            <div
              className='scrollbar-hide flex snap-x snap-mandatory gap-4 overflow-x-auto'
              id={`container-${group.date}`}
              ref={(el) => {
                // Scroll to end on mount for groups with more than 4 items
                if (el && group.items.length > 4) {
                  el.scrollLeft = el.scrollWidth;
                }
              }}
            >
              {group.items.map((snap, index) => (
                <div
                  key={index}
                  className='relative w-[calc(50%-8px)] flex-none snap-start overflow-hidden rounded-2xl border border-gray-200 shadow-sm md:w-[calc(25%-12px)]'
                >
                  <div className='relative aspect-video'>
                    <Image
                      src={snap.thumbnail}
                      alt='강의 썸네일'
                      fill
                      sizes='(max-width: 768px) 50vw, 25vw'
                      className='object-cover'
                    />
                  </div>
                  <div className='p-4'>
                    <div className='sm:text-md text-xs font-semibold text-gray-950'>
                      스냅 N컷
                    </div>
                    <p className='sm:text-md line-clamp-2 text-xs text-gray-950'>
                      {snap.title}
                    </p>
                  </div>
                </div>
              ))}
            </div>
            {group.items.length > 4 && (
              <div className='pointer-events-none absolute top-1/2 flex w-full -translate-y-1/2 justify-between px-4'>
                <button
                  onClick={() => {
                    const container = document.getElementById(
                      `container-${group.date}`,
                    ) as HTMLDivElement;
                    if (container) handleScroll(container, 'left');
                  }}
                  className='pointer-events-auto rounded-full bg-white p-2 shadow hover:bg-gray-50'
                >
                  <svg
                    width='24'
                    height='24'
                    viewBox='0 0 24 24'
                    fill='none'
                    className='text-blue-400'
                  >
                    <path
                      d='M15 19l-7-7 7-7'
                      stroke='currentColor'
                      strokeWidth='2'
                      strokeLinecap='round'
                      strokeLinejoin='round'
                    />
                  </svg>
                </button>
                <button
                  onClick={() => {
                    const container = document.getElementById(
                      `container-${group.date}`,
                    ) as HTMLDivElement;
                    if (container) handleScroll(container, 'right');
                  }}
                  className='pointer-events-auto rounded-full bg-white p-2 shadow hover:bg-gray-50'
                >
                  <svg
                    width='24'
                    height='24'
                    viewBox='0 0 24 24'
                    fill='none'
                    className='text-gray-200'
                  >
                    <path
                      d='M9 5l7 7-7 7'
                      stroke='currentColor'
                      strokeWidth='2'
                      strokeLinecap='round'
                      strokeLinejoin='round'
                    />
                  </svg>
                </button>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export default SnapCard;
