'use client';

import { getContentList } from '@/api/content';
import { useInfiniteScroll } from '@/hooks/useInfiniteScroll';
import { Content } from '@/types/content';
import ContentsLoading from './ContentsLoading';
import MyContentCard from './MyContentCard';

const MyContentContainer = () => {
  const {
    data: contents,
    isFetchingNextPage,
    isLoading,
    observerTarget,
  } = useInfiniteScroll<Content>({
    queryKey: ['contents'],
    queryFn: async (page, pageSize) => {
      const data = await getContentList({
        p_pageno: page,
        p_pagesize: pageSize,
      });
      return {
        data: data.content,
        hasNextPage: data.hasNext,
      };
    },
    pageSize: 10,
  });

  if (isLoading) {
    return <ContentsLoading />;
  }

  return (
    <div className='flex w-full flex-col gap-4 xl:gap-8'>
      <div className='grid w-full grid-cols-1 gap-4 md:grid-cols-2 xl:gap-8 2xl:grid-cols-3'>
        {contents &&
          contents.length > 0 &&
          contents.map((item) => (
            <MyContentCard
              key={item.contentSeq}
              contentSeq={item.contentSeq}
              title={item.title}
              completedAt={item.completedAt}
              thumbnailUrl={item.thumbnailUrl}
              processStatus={item.processStatus}
            />
          ))}
      </div>
      <div ref={observerTarget} className='h-4 w-full'>
        {isFetchingNextPage && <div>Spinner</div>}
      </div>
    </div>
  );
};

export default MyContentContainer;
