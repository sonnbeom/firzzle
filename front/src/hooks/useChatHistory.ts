import {
  InfiniteData,
  useQueryClient,
  useSuspenseInfiniteQuery,
} from '@tanstack/react-query';
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
  const queryClient = useQueryClient();

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    error,
    refetch,
  } = useSuspenseInfiniteQuery({
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

  // 낙관적 업데이트를 위한 메서드
  const addOptimisticChat = (newChat: T) => {
    queryClient.setQueryData<InfiniteData<T[]>>(queryKey, (oldData) => {
      if (!oldData) return { pages: [[newChat]], pageParams: [undefined] };

      const newPages = [...oldData.pages];
      if (newPages.length > 0) {
        newPages[0] = [newChat, ...newPages[0]];
      } else {
        newPages.push([newChat]);
      }

      return {
        ...oldData,
        pages: newPages,
      };
    });
  };

  // 무한 스크롤 처리
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
    refetch,
    addOptimisticChat,
  };
}
