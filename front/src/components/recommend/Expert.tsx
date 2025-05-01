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
      <div className='mt-4 grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3'>
        {visibleExperts.map((item, idx) => (
          <div
            key={idx}
            className='flex h-[320px] w-full flex-col items-center rounded-xl bg-white p-6 shadow-md transition-transform'
          >
            <div className='relative mb-3 h-24 w-24 flex-shrink-0 overflow-hidden rounded-full border-2 border-blue-200'>
              <Image
                src={item.thumbnail}
                alt={`${item.name} 전문가 이미지`}
                width={96}
                height={96}
                className='h-full w-full object-contain'
              />
            </div>
            <h3 className='mb-2 flex-shrink-0 text-lg font-bold text-gray-800'>
              {item.name}
            </h3>
            <div className='mb-3 h-[120px] w-full'>
              <p className='text-center text-sm text-gray-600'>
                {item.description}
              </p>
            </div>
            <a
              href={item.url}
              target='_blank'
              rel='noopener noreferrer'
              className='mt-2 rounded-full bg-blue-50 px-4 py-1.5 text-sm text-blue-500 hover:bg-blue-100'
            >
              프로필 보기
            </a>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Expert;
