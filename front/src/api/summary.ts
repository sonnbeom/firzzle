import { ApiResponseWithData } from '@/types/common/apiResponse';
import { SummaryData } from '@/types/summary';
import { externalApi } from './common/apiInstance';

// 임시 응답 타입
interface SummaryResponse {
  easyData: SummaryData[];
  highData: SummaryData[];
}

// 요약 조회
export const getSummary = (contentId: string) => {
  return externalApi.get(`/contents/${contentId}/summary`) as Promise<
    ApiResponseWithData<SummaryResponse>
  >;
};
