import { http, HttpResponse } from 'msw';
import { SummaryDetail } from '@/types/summary';

// 임시 응답 타입
interface SummaryResponse {
  data: {
    easyData: SummaryDetail[];
    highData: SummaryDetail[];
  };
}

export const summaryHandlers = [
  // 요약 조회
  http.get('/contents/:contentId/summary', async ({ params }) => {
    const response: SummaryResponse = {
      data: {
        easyData: [
          {
            sectionSeq: '1',
            title: '01 머신러닝 개요',
            startTime: 95,
            details: '머신러닝의 개요를 알아보자',
          },
        ],
        highData: [
          {
            sectionSeq: '1',
            title: '01 머신러닝 개요',
            startTime: 95,
            details: '머신러닝의 개요를 핵심만 알아보자',
          },
        ],
      },
    };
    return HttpResponse.json(response);
  }),
];
