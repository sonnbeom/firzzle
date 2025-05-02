import React from 'react';
import Expert from '@/components/recommend/Expert';
import Lecture from '@/components/recommend/Lecture';

const keyword = '인공지능';

const baseLectures = [
  {
    title: '인공지능 기초와 활용 - 입문자를 위한 강의',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/watch?v=lgiMrbLS3pk',
  },
  {
    title: '딥러닝 프로젝트 실습 강좌',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/',
  },
  {
    title: '머신러닝 알고리즘 완전 정복',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/',
  },
];

const lectureList = [
  ...baseLectures,
  ...Array(9).fill({
    title: '인공지능과 빅데이터 분석 실무 강의',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/',
  }),
];

const expertList = [
  {
    name: '홍길동',
    description: 'HR Planning & Analytics, Design Thinking 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
  },
  {
    name: '김전문',
    description: '인공지능 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
  },
  {
    name: '박박사',
    description: '데이터 분석 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
  },
  {
    name: '이교수',
    description: 'AI 분석 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
  },
];

async function RecommendPage() {
  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      <div className='space-y-10 pb-20'>
        <Lecture lectures={lectureList} keyword={keyword} />
        <Expert experts={expertList} keyword={keyword} />
      </div>
    </div>
  );
}

export default RecommendPage;
