import { http, HttpResponse } from 'msw';
import { RecommendLecture, ExpertRecommend } from '@/types/recommend';

const recommendLectures: RecommendLecture[] = [
  {
    title: '인공지능 기초와 활용 - 입문자를 위한 강의',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/watch?v=lgiMrbLS3pk',
    keyword: '인공지능',
  },
  {
    title: '딥러닝 프로젝트 실습 강좌',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/',
    keyword: '인공지능',
  },
  {
    title: '머신러닝 알고리즘 완전 정복',
    thumbnail: '/assets/images/Firzzle.png',
    url: 'https://www.youtube.com/',
    keyword: '인공지능',
  },
];

const expertRecommends: ExpertRecommend[] = [
  {
    name: '홍길동',
    description: 'HR Planning & Analytics, Design Thinking 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
    keyword: '인공지능',
  },
  {
    name: '김전문',
    description: '인공지능 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
    keyword: '인공지능',
  },
  {
    name: '박박사',
    description: '데이터 분석 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
    keyword: '인공지능',
  },
  {
    name: '이교수',
    description: 'AI 분석 전문가',
    thumbnail: '/assets/images/AI Playground.png',
    url: 'https://kr.linkedin.com/',
    keyword: '인공지능',
  },
];

export const recommendHandlers = [
  // 추천 강의 조회 API
  http.get('/contents/:contentId/recommendations', () => {
    const response = {
      data: recommendLectures,
    };
    return HttpResponse.json(response);
  }),

  // 전문가 추천 조회 API
  http.get('/contents/:contentId/expert-recommendations', () => {
    const response = {
      data: expertRecommends,
    };
    return HttpResponse.json(response);
  }),
];
