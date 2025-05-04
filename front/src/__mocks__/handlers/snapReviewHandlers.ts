import { http, HttpResponse } from 'msw';
import { DateGroup, Review } from '@/types/snapReview';

const snapBookData: DateGroup[] = [
  {
    date: '2025년 4월 25일',
    items: Array(6).fill({
      id: 'snap1',
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      youtubeUrl: 'https://www.youtube.com/watch?v=1TdotRJBz_U',
      date: '2025년 4월 25일',
      length: 4,
    }),
  },
  {
    date: '2025년 4월 24일',
    items: Array(4).fill({
      id: 'snap2',
      title: '리액트 훅스 완벽 정리',
      youtubeUrl: 'https://www.youtube.com/watch?v=1TdotRJBz_U',
      date: '2025년 4월 24일',
      length: 6,
    }),
  },
  {
    date: '2025년 4월 23일',
    items: Array(4).fill({
      id: 'snap3',
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      youtubeUrl: 'https://www.youtube.com/watch?v=1TdotRJBz_U',
      date: '2025년 4월 23일',
      length: 3,
    }),
  },
];

const mockReview: Review = {
  title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
  date: '2025.04.23',
  images: [
    {
      src: '/assets/images/Firzzle.png',
      description: null,
      timestamp: 30,
    },
    {
      src: '/assets/images/Firzzle.png',
      description: null,
      timestamp: 30,
    },
    {
      src: '/assets/images/Firzzle.png',
      description: '강화학습의 기본 개념과 피드백 학습 방식을 소개합니다.',
      timestamp: 30,
    },
    {
      src: '/assets/images/Firzzle.png',
      description: '강화학습의 기본 개념과 피드백 학습 방식을 소개합니다.',
      timestamp: 30,
    },
  ],
};

export const handlers = [
  // 스냅북 목록 조회
  http.get('/snap-reviews', () => {
    return HttpResponse.json(snapBookData);
  }),

  // 개별 스냅 리뷰 조회회
  http.get('/contents/:contentId/snap-review', () => {
    return HttpResponse.json(mockReview);
  }),
];
