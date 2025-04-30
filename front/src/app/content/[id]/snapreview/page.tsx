import React from 'react';
import ReviewCard from '@/components/snapreview/ReviewCard';

// 임시 데이터 (나중에 API 연동)
const reviews = Array(4)
  .fill(null)
  .map((_, index) => ({
    id: String(index),
    description: null,
    thumbnail: '/assets/images/Firzzle.png',
    timestamp: 100,
  }));

async function SnapReviewPage() {
  return (
    <div className='relative min-h-screen w-full px-2 md:px-4'>
      <div className='space-y-10 pb-20'>
        <ReviewCard reviews={reviews} />
      </div>
    </div>
  );
}

export default SnapReviewPage;
