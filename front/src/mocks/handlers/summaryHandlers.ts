import { http, HttpResponse } from 'msw';
import { SummaryData } from '@/types/summary';

// 임시 응답 타입
interface SummaryResponse {
  data: {
    easyData: SummaryData[];
    highData: SummaryData[];
  };
}

export const summaryHandlers = [
  // 요약 조회
  http.get('/contents/:contentId/summary', async ({ params }) => {
    console.log('MSW: Handling request for summary with params:', params);
    const response: SummaryResponse = {
      data: {
        easyData: [
          {
            id: '1',
            title: '01 머신러닝 개요',
            description: '머신러닝의 개요를 알아보자',
            time: 95,
          },
        ],
        highData: [
          {
            id: '1',
            title: '01 머신러닝 개요',
            description: '머신러닝의 개요를 핵심만 알아보자',
            time: 95,
          },
        ],
      },
    };
    return HttpResponse.json(response);
  }),
];
