import { api } from './common/apiInstance';

// 요약 조회
export const getSummary = (contentId: string) => {
  const respose = api.get(`/contents/${contentId}/summary`);
  return respose;
};
