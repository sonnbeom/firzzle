import { useInfiniteQuery } from '@tanstack/react-query';
import { useEffect, useRef } from 'react';
import { LearningChat } from '@/types/learningChat';

interface UseChatHistoryProps<T> {
  queryKey: string[];
  queryFn: (lastIndate?: string) => Promise<T[]>;
}

export function useChatHistory<T extends LearningChat>({
  queryKey,
  queryFn,
}: UseChatHistoryProps<T>) {
  const observerTarget = useRef<HTMLDivElement>(null);

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    error,
  } = useInfiniteQuery({
    queryKey,
    queryFn: async ({ pageParam }) => {
      const response = await queryFn(pageParam);
      return response;
    },
    getNextPageParam: (lastPage) => {
      if (lastPage.length === 0) return undefined;
      return lastPage[lastPage.length - 1].indate;
    },
    initialPageParam: undefined,
  });

  useEffect(() => {
    if (!hasNextPage || isFetchingNextPage) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 },
    );

    if (observerTarget.current) {
      observer.observe(observerTarget.current);
    }

    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const chatHistory = data?.pages.flat() ?? [];

  return {
    data: chatHistory,
    isLoading,
    error,
    hasNextPage,
    fetchNextPage,
    observerTarget,
  };
}
