import { SummaryData } from '@/types/summary';
import { api } from './common/apiInstance';

// 임시 응답 타입
interface SummaryResponse {
  easyData: SummaryData[];
  highData: SummaryData[];
}

// 요약 조회
export const getSummary = (contentId: string) => {
  return api.get<SummaryResponse>(`/contents/${contentId}/summary`);
};
