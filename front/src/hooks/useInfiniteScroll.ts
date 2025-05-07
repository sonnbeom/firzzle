import { useInfiniteQuery } from '@tanstack/react-query';
import { useEffect, useRef } from 'react';

interface UseInfiniteScrollProps<T> {
  queryKey: string[];
  queryFn: (
    pageParam: number,
    pageSize: number,
  ) => Promise<{
    data: T[];
    hasNextPage: boolean;
  }>;
  enabled?: boolean;
  pageSize?: number;
}

export function useInfiniteScroll<T>({
  queryKey,
  queryFn,
  enabled = true,
  pageSize = 20,
}: UseInfiniteScrollProps<T>) {
  // Intersection Observer의 타겟 요소에 대한 ref
  const observerTarget = useRef<HTMLDivElement>(null);

  // 무한 스크롤 쿼리
  const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useInfiniteQuery({
      queryKey: [...queryKey, pageSize],
      queryFn: ({ pageParam = 1 }) => queryFn(pageParam, pageSize),
      getNextPageParam: (lastPage, pages) =>
        lastPage.hasNextPage ? pages.length + 1 : undefined,
      initialPageParam: 1,
      enabled,
    });

  // 모든 아이템 데이터
  const allItems = data?.pages.flatMap((page) => page.data) || [];

  useEffect(() => {
    // Intersection Observer 생성
    // 타겟 요소가 뷰포트와 교차하는지 관찰
    const observer = new IntersectionObserver(
      (entries) => {
        // 타겟이 뷰포트와 교차하고, 더 불러올 데이터가 있으며, 현재 로딩 중이 아닐 때
        if (entries[0]?.isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { threshold: 0.1 }, // 타겟 요소가 10% 이상 보일 때 콜백 실행
    );

    // 옵저버 타겟 요소가 있으면 관찰 시작
    if (observerTarget.current) {
      observer.observe(observerTarget.current);
    }

    // 컴포넌트 언마운트 시 옵저버 해제
    return () => observer.disconnect();
  }, [fetchNextPage, hasNextPage, isFetchingNextPage]);

  return {
    data: allItems,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    observerTarget,
  };
}
