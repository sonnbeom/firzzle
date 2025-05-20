'use client';

import { useParams } from 'next/navigation';
import { useCallback, useEffect, useState } from 'react';
import { getRecommendations } from '@/api/recommend';
import { usePagination } from '@/hooks/usePagination';
import { VideoProps } from '@/types/recommend';
import Icons from '../common/Icons';
import LectureCard from './LectureCard';

const Lecture = () => {
  const params = useParams();
  const contentId = params?.id as string;
  const [lectures, setLectures] = useState<VideoProps[]>([]);
  const [keyword, setKeyword] = useState('');
  const [totalItems, setTotalItems] = useState(0);
  const itemsPerPage = 6;

  const fetchLectures = useCallback(
    async (page: number) => {
      try {
        const response = await getRecommendations(contentId, {
          p_pageno: page,
          p_pagesize: itemsPerPage,
        });
        const videos = response.content.map((item) => ({
          title: item.title,
          url: item.url,
          thumbnailUrl: item.thumbnailUrl,
        }));

        setLectures(videos);
        setTotalItems(response.totalElements);
        setKeyword(response.originTags);
      } catch (error) {
        console.error('Failed to fetch lectures:', error);
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
    onPageChange: fetchLectures,
  });

  useEffect(() => {
    fetchLectures(1);
  }, [contentId, fetchLectures]);
  return (
    <div>
      <h2 className='text-center text-lg font-medium text-gray-900 md:text-xl'>
        <span className='font-semibold text-blue-400'>{keyword}</span> 관련된
        강의를 추천해드릴게요
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
      {lectures.length > 0 ? (
        <div className='grid grid-cols-3 gap-5 pt-5'>
          {lectures.map((video, idx) => (
            <LectureCard key={idx} video={video} />
          ))}
        </div>
      ) : (
        <div className='pt-5'>
          <div
            className='relative w-full overflow-hidden rounded-lg border border-gray-200 bg-blue-50'
            style={{ aspectRatio: '16/4.5' }}
          >
            <div className='flex h-full items-center justify-center text-center text-gray-500'>
              현재는 관련 강의가 없습니다.
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Lecture;
