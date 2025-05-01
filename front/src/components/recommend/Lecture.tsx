'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import React from 'react';
import { usePagination } from '@/hooks/usePagination';
import Icons from '../common/Icons';

interface LectureProps {
  lectures: Array<{
    title: string;
    thumbnail: string;
    url: string;
  }>;
  keyword: string;
}

const Lecture = ({ lectures, keyword }: LectureProps) => {
  const router = useRouter();
  const {
    visibleItems: visibleLectures,
    showPagination,
    canGoPrev,
    canGoNext,
    handlePrevPage,
    handleNextPage,
  } = usePagination({
    items: lectures,
    itemsPerPage: 6,
  });

  return (
    <div>
      <h2 className='text-center text-lg font-medium text-gray-900 sm:text-xl'>
        <span className='font-semibold text-blue-400'>{keyword}</span>에 관련된
        강의를 추천해드릴게요
      </h2>
      {showPagination && (
        <div className='flex justify-end'>
          <button onClick={handlePrevPage}>
            <Icons
              id='arrow-left'
              size={24}
              color={canGoPrev ? 'text-blue-400' : 'text-gray-200'}
            />
          </button>
          <button onClick={handleNextPage}>
            <Icons
              id='arrow-right'
              size={24}
              color={canGoNext ? 'text-blue-400' : 'text-gray-200'}
            />
          </button>
        </div>
      )}
      <div className='grid grid-cols-2 gap-5 pt-5 sm:grid-cols-3'>
        {visibleLectures.map((item, idx) => (
          <div
            key={idx}
            onClick={() =>
              router.push(`/content?url=${encodeURIComponent(item.url)}`)
            }
            className='block cursor-pointer hover:opacity-80'
          >
            <div className='relative aspect-video w-full max-w-[300px] overflow-hidden rounded-lg border border-gray-200'>
              <Image
                src={item.thumbnail}
                alt='강의 썸네일'
                fill
                sizes='(max-width: 300px) 100vw, 300px'
                className='object-cover'
              />
            </div>
            <p className='mt-2 text-sm text-gray-700'>{item.title}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Lecture;
