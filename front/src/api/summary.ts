import { SummaryResponse } from '@/types/summary';
import { externalApi } from './common/apiInstance';

// 요약 조회
export const getSummary = async (contentSeq: string) => {
  const { data } = await externalApi.get<SummaryResponse>(
    `/learning/contents/${contentSeq}/summary`,
  );
  return data;
};
