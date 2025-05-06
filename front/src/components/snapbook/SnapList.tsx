'use client';

import Link from 'next/link';
import { SnapReviewListResponse } from '@/types/snapReview';
import SnapCard from './SnapCard';

interface SnapListProps {
  initialData: SnapReviewListResponse;
}

function SnapList({ initialData }: SnapListProps) {
  // 날짜별 스냅리뷰 추출
  const dailyReviews = initialData.content[0].dailySnapReviews;

  return (
    <div className='MB container mx-auto px-4'>
      {Object.entries(dailyReviews)
        .sort((a, b) => b[0].localeCompare(a[0])) // 날짜 역순 정렬
        .map(([date, reviews]) => (
          <div key={date} className='mb-16'>
            <h2 className='mb-4 text-lg text-gray-950 md:text-xl'>
              {new Date(date).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </h2>
            <div className='grid grid-cols-2 gap-4 md:grid-cols-4'>
              {reviews.map((review) => (
                <Link
                  key={review.contentSeq}
                  href={`/content/snapbook/${review.contentSeq}`}
                  className='block'
                >
                  <SnapCard data={review} />
                </Link>
              ))}
            </div>
          </div>
        ))}
    </div>
  );
}

export default SnapList;
