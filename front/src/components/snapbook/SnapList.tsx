'use client';

import { getSnapReviews } from '@/api/snap';
import { useInfiniteScroll } from '@/hooks/useInfiniteScroll';
import { SnapReviewListResponse } from '@/types/snapReview';
import BasicToaster from '../common/BasicToaster';
import LoadingSpinner from '../common/LoadingSpinner';
import SnapBookLoading from './SnapBookLoading';
import SnapDateGroup from './SnapDateGroup';

const SnapList = () => {
  const EMPTY_SNAP_REVIEW: SnapReviewListResponse = {
    content: [
      {
        dailySnapReviews: {},
        totalDays: 0,
      },
    ],
    p_pageno: 1,
    p_pagesize: 10,
    totalElements: 0,
    totalPages: 0,
    last: true,
    hasNext: false,
  };

  const {
    data: snapReviews,
    isFetchingNextPage,
    isLoading,
    observerTarget,
  } = useInfiniteScroll<SnapReviewListResponse>({
    queryKey: ['snapReviews'],
    queryFn: async (page, pageSize) => {
      try {
        const response = await getSnapReviews({
          p_pageno: page,
          p_pagesize: pageSize,
          p_order: 'indate',
          p_sortorder: 'desc',
        });
        return {
          data: [response],
          hasNextPage: !response.last,
        };
      } catch (error) {
        BasicToaster.error(error.message, {
          id: 'snapbook',
          duration: 2000,
        });
        return {
          data: [EMPTY_SNAP_REVIEW],
          hasNextPage: false,
        };
      }
    },
    pageSize: 10,
  });

  if (isLoading) {
    return <SnapBookLoading />;
  }

  if (!snapReviews || snapReviews.length === 0) {
    return (
      <div className='flex min-h-[50vh] items-center justify-center'>
        <p className='text-md text-center text-gray-600 md:text-lg'>
          작성한 스냅 리뷰가 없습니다.
        </p>
      </div>
    );
  }

  return (
    <div className='container mx-auto'>
      {snapReviews?.map((response, responseIndex) => {
        const reviews = Object.values(
          response.content[0]?.dailySnapReviews || {},
        ).flat();
        return (
          <SnapDateGroup
            key={responseIndex}
            reviews={reviews}
            isPriorityPage={responseIndex === 0}
          />
        );
      })}
      {isFetchingNextPage ? (
        <LoadingSpinner />
      ) : (
        <div ref={observerTarget} className='h-10' />
      )}
    </div>
  );
};

export default SnapList;
