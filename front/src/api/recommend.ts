import { SnapReviewListResponse, ExpertRecommend } from '@/types/recommend';
import { externalApi } from './common/apiInstance';

// 추천 강의 조회
export const getRecommendations = async (contentSeq: number,  p_pageno?: number,
  p_pagesize?: number,
  p_order?: string,
  p_sortorder?: string,
  keyword?: string,
  category?: string,
status?: string,
) => {
  const {data} = await externalApi.get<SnapReviewListResponse>(`/learning/contents/${contentSeq}/recommendations`,{    
    params: {
    p_pageno,
    p_pagesize,
    p_order,
    p_sortorder,
    keyword,
    category,
    status,
  },});
  return data;
};

// 전문가 추천 조회
export const getExpertRecommendations = (contentId: string) => {
  return externalApi.get<ExpertRecommend[]>(`/contents/${contentId}/expert-recommendations`);
};
