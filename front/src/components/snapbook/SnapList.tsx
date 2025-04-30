'use client';

import Link from 'next/link';
import { useEffect, useRef, useState } from 'react';
import { ITEMS_PER_PAGE } from 'utils/const';
import SnapCard from './SnapCard';

interface SnapItem {
  id: string;
  title: string;
  thumbnail: string;
  date: string;
  length: number;
}

interface DateGroup {
  date: string;
  items: SnapItem[];
}

interface SnapListProps {
  initialGroups: DateGroup[];
}

function SnapList({ initialGroups }: SnapListProps) {
  const [visibleGroups, setVisibleGroups] = useState<DateGroup[]>(
    initialGroups.slice(0, ITEMS_PER_PAGE),
  );

  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(initialGroups.length > ITEMS_PER_PAGE);

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
          const start = page * ITEMS_PER_PAGE; // 시작 인덱스
          const end = start + ITEMS_PER_PAGE; // 끝 인덱스
          const newGroups = initialGroups.slice(start, end);

          // 새로운 데이터가 없으면 더 이상 로드하지 않음
          if (newGroups.length === 0) {
            setHasMore(false);
          } else {
            // 기존 데이터에 새로운 데이터 추가
            setVisibleGroups((prev) => [...prev, ...newGroups]);
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
  }, [page, hasMore, loading, initialGroups]);

  return (
    <div className='MB container mx-auto px-4'>
      {visibleGroups.map((group) => (
        <div key={group.date} className='mb-16'>
          <h2 className='mb-4 text-lg text-gray-950 md:text-xl'>
            {group.date}
          </h2>
          <div className='grid grid-cols-2 gap-4 md:grid-cols-4'>
            {group.items.map((item, index) => (
              <Link
                key={`${item.id}-${index}`}
                href={`/content/snapbook/${item.id}`}
                className='block'
              >
                <SnapCard data={item} />
              </Link>
            ))}
          </div>
        </div>
      ))}
      <div ref={observerTarget} />
    </div>
  );
}

export default SnapList;
