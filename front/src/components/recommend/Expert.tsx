'use client';

import { useParams } from 'next/navigation';
import { useCallback, useEffect, useState } from 'react';
import { getExpertRecommendations } from '@/api/recommend';
import { usePagination } from '@/hooks/usePagination';
import { Expert as ExpertType } from '@/types/recommend';
import Icons from '../common/Icons';
import ExpertCard from './ExpertCard';

const Expert = () => {
  const params = useParams();
  const contentId = params?.id as string;
  const [experts, setExperts] = useState<ExpertType[]>([]);
  const [keyword, setKeyword] = useState('');
  const [totalItems, setTotalItems] = useState(0);
  const itemsPerPage = 3;

  const fetchExperts = useCallback(
    async (page: number) => {
      try {
        const response = await getExpertRecommendations(contentId, {
          p_pageno: page,
          p_pagesize: itemsPerPage,
        });
        setExperts(response.content);
        setTotalItems(response.totalElements);
        setKeyword(response.originTags);
      } catch (error) {
        console.error('Failed to fetch experts:', error);
      }
    },
    [contentId, itemsPerPage],
  );

  const {
    canGoPrev,
    canGoNext,
    showPagination,
    handlePrevPage,
    handleNextPage,
  } = usePagination({
    itemsPerPage,
    totalItems,
    onPageChange: fetchExperts,
  });

  useEffect(() => {
    fetchExperts(1);
  }, [contentId, fetchExperts]);

  return (
    <div className='mt-8'>
      <h2 className='text-center text-lg font-medium text-gray-900 md:text-xl'>
        <span className='font-semibold text-blue-400'>{keyword}</span> 관련된
        전문가를 추천해드릴게요
      </h2>
      {showPagination && (
        <div className='flex justify-end'>
          <button onClick={handlePrevPage}>
            <Icons
              id='arrow-fill-left'
              size={24}
              color={canGoPrev ? 'text-blue-400' : 'text-gray-200'}
            />
          </button>
          <button onClick={handleNextPage}>
            <Icons
              id='arrow-fill-right'
              size={24}
              color={canGoNext ? 'text-blue-400' : 'text-gray-200'}
            />
          </button>
        </div>
      )}
      {experts.length > 0 ? (
        <div className='mx-auto grid w-full max-w-[1000px] grid-cols-3 gap-1 px-1 sm:gap-2 sm:px-2 md:gap-4 md:px-6'>
          {experts.map((expert) => (
            <ExpertCard key={expert.expertSeq} expert={expert} />
          ))}
        </div>
      ) : (
        <div className='pt-5'>
          <div
            className='relative w-full overflow-hidden rounded-lg border border-gray-200 bg-blue-50'
            style={{ aspectRatio: '16/4.5' }}
          >
            <div className='flex h-full items-center justify-center text-center text-gray-500'>
              현재는 관련 전문가가 없습니다.
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Expert;
