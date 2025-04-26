import React from 'react';

// components
// import SnapCard from '@/components/snapreview/SnapCard';
import Review from '@/components/snapreview/Review';

// Mock data structure
interface SnapItem {
  title: string;
  thumbnail: string;
  date: string;
}

interface DateGroup {
  date: string;
  items: SnapItem[];
}

// Mock data
const mockData: DateGroup[] = [
  {
    date: '2025년 4월 23일',
    items: Array(6).fill({
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      thumbnail: '/assets/images/Firzzle.png',
      date: '2025년 4월 23일',
    }),
  },
  {
    date: '2025년 4월 24일',
    items: Array(3).fill({
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      thumbnail: '/assets/images/Firzzle.png',
      date: '2025년 4월 24일',
    }),
  },
  {
    date: '2025년 4월 25일',
    items: Array(4).fill({
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      thumbnail: '/assets/images/Firzzle.png',
      date: '2025년 4월 25일',
    }),
  },
];

const SnapBookPage = () => {
  // Add error boundary
  if (!mockData || !Array.isArray(mockData)) {
    console.error('Invalid data format:', mockData);
    return <div>Error: Invalid data format</div>;
  }

  return (
    <div>
      {/*<SnapCard data={mockData} />*/}
      <Review />
    </div>
  );
};

export default SnapBookPage;
