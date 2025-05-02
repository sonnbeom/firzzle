import { RecommendLecture, ExpertRecommend } from '@/types/recommend';
import { externalApi } from './common/apiInstance';

// 추천 강의 조회
export const getRecommendations = (contentId: string) => {
  return externalApi.get<RecommendLecture[]>(`/contents/${contentId}/recommendations`);
};

// 전문가 추천 조회
export const getExpertRecommendations = (contentId: string) => {
  return externalApi.get<ExpertRecommend[]>(`/contents/${contentId}/expert-recommendations`);
};
