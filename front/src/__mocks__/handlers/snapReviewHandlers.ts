import { http, HttpResponse } from 'msw';
import { DateGroup } from '@/types/snapReview';

const snapBookData: DateGroup[] = [
  {
    date: '2025년 4월 25일',
    items: Array(6).fill({
      id: 'snap1',
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      thumbnail: '/assets/images/Firzzle.png',
      date: '2025년 4월 25일',
      length: 4,
    }),
  },
  {
    date: '2025년 4월 24일',
    items: Array(4).fill({
      id: 'snap2',
      title: '리액트 훅스 완벽 정리',
      thumbnail: '/assets/images/Firzzle.png',
      date: '2025년 4월 24일',
      length: 6,
    }),
  },
  {
    date: '2025년 4월 23일',
    items: Array(4).fill({
      id: 'snap3',
      title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
      thumbnail: '/assets/images/Firzzle.png',
      date: '2025년 4월 23일',
      length: 3,
    }),
  },
];

export const snapListhandlers = [
  http.get('/snap-reviews', () => {
    return HttpResponse.json({ data: snapBookData });
  }),
];
