import { useEffect, useRef, useState } from 'react';

interface UseInfiniteScrollProps<T> {
  initialData: T[];
  itemsPerPage: number;
}

interface UseInfiniteScrollReturn<T> {
  visibleData: T[];
  observerTarget: React.RefObject<HTMLDivElement>;
}

export function useInfiniteScroll<T>({
  initialData,
  itemsPerPage,
}: UseInfiniteScrollProps<T>): UseInfiniteScrollReturn<T> {
  const [visibleData, setVisibleData] = useState<T[]>(
    initialData.slice(0, itemsPerPage),
  );
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(initialData.length > itemsPerPage);

  // Intersection Observer의 타겟 요소에 대한 ref
  const observerTarget = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Intersection Observer 생성
    // 타겟 요소가 뷰포트와 교차하는지 관찰
    const observer = new IntersectionObserver(
      (entries) => {
        // 타겟이 뷰포트와 교차하고, 더 불러올 데이터가 있으며, 현재 로딩 중이 아닐 때
        if (entries[0].isIntersecting && hasMore && !loading) {
          setLoading(true);

          // 다음 페이지 데이터 계산
          const nextPage = page + 1;
          const start = page * itemsPerPage; // 시작 인덱스
          const end = start + itemsPerPage; // 끝 인덱스
          const newItems = initialData.slice(start, end);

          // 새로운 데이터가 없으면 더 이상 로드하지 않음
          if (newItems.length === 0) {
            setHasMore(false);
          } else {
            // 기존 데이터에 새로운 데이터 추가
            setVisibleData((prev) => [...prev, ...newItems]);
            setPage(nextPage);
          }
          setLoading(false);
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
  }, [page, hasMore, loading, initialData, itemsPerPage]);

  return { visibleData, observerTarget };
}
