import React from 'react';

//components
import ReviewCard from '@/components/snapreview/ReviewCard';

const SnapReviewPage = () => {
  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      <div className='space-y-10 pb-20'>
        <ReviewCard />
      </div>
    </div>
  );
};
export default SnapReviewPage;
