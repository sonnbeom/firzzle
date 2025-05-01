'use client';

import Image from 'next/image';
import React from 'react';
import { usePagination } from '@/hooks/usePagination';
import Icons from '../common/Icons';

interface ExpertProps {
  experts: Array<{
    name: string;
    description: string;
    thumbnail: string;
    url: string;
  }>;
  keyword: string;
}

const Expert = ({ experts, keyword }: ExpertProps) => {
  const {
    visibleItems: visibleExperts,
    showPagination,
    canGoPrev,
    canGoNext,
    handlePrevPage,
    handleNextPage,
  } = usePagination({
    items: experts,
    itemsPerPage: 3,
  });

  return (
    <div>
      <h2 className='text-center text-lg font-medium text-gray-900 sm:text-xl'>
        <span className='font-semibold text-blue-400'>{keyword}</span> 전문가와
        대화해보세요
      </h2>
      {showPagination && (
        <div className='flex justify-end'>
          <button onClick={handlePrevPage} disabled={!canGoPrev}>
            <Icons
              id='arrow-left'
              size={24}
              color={canGoPrev ? 'text-blue-400' : 'text-gray-200'}
            />
          </button>
          <button onClick={handleNextPage} disabled={!canGoNext}>
            <Icons
              id='arrow-right'
              size={24}
              color={canGoNext ? 'text-blue-400' : 'text-gray-200'}
            />
          </button>
        </div>
      )}
      <div className='mt-4 grid grid-cols-2 gap-4 sm:grid-cols-3'>
        {visibleExperts.map((item, idx) => (
          <div
            key={idx}
            className='flex flex-col items-center rounded-xl p-4 shadow'
            style={{
              background:
                'linear-gradient(to bottom, #324eef 50%, #ffffff 50%)',
            }}
          >
            <div className='relative h-[100px] w-[100px] overflow-hidden rounded-full border border-gray-200'>
              <Image
                src={item.thumbnail}
                alt='전문가 이미지'
                fill
                sizes='100px'
                className='object-cover'
              />
            </div>
            <p className='mt-2 font-semibold text-gray-700'>{item.name}</p>
            <p className='mt-2 mb-2 text-center text-xs text-gray-700'>
              {item.description}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Expert;
