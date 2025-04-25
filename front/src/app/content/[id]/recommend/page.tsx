import React from 'react';
import Expert from '@/components/recommend/Expert';
import Lecture from '@/components/recommend/Lecture';

const RecommendPage = () => {
  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      <div className='space-y-10 pb-20'>
        <Lecture />
        <Expert />
      </div>
    </div>
  );
};

export default RecommendPage;
