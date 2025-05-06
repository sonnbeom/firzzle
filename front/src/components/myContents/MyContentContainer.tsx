'use client';

import { getContentList } from '@/api/content';
import { useInfiniteScroll } from '@/hooks/useInfiniteScroll';
import { Content } from '@/types/content';
import MyContentCard from './MyContentCard';

const MyContentContainer = () => {
  const {
    data: contents,
    isLoading,
    isFetchingNextPage,
    observerTarget,
  } = useInfiniteScroll<Content>({
    queryKey: ['contents'],
    queryFn: async (page, pageSize) => {
      const data = await getContentList(page, pageSize);
      return {
        data: data.content,
        hasNextPage: data.hasNext,
      };
    },
    pageSize: 10,
  });

  return (
    <div className='flex w-full flex-col gap-8'>
      <div className='grid w-full grid-cols-2 gap-8'>
        {contents &&
          contents.length > 0 &&
          contents.map((item) => (
            <MyContentCard
              key={item.contentSeq}
              contentSeq={item.contentSeq}
              title={item.title}
              completedAt={item.completedAt}
              thumbnailUrl={item.thumbnailUrl}
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
