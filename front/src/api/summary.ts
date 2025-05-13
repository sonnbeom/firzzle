import { SummaryResponse } from '@/types/summary';
import { api } from './common/apiInstance';

// 요약 조회
export const getSummary = async (contentSeq: string) => {
  const { data } = await api.get<SummaryResponse>(
    `/learning/contents/${contentSeq}/summary`,
  );
  return data;
};
