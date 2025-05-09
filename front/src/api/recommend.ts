import { InfiniteScrollRequest } from '@/types/common';
import {
  SnapReviewListResponse,
  ExpertListResponse,
  ExpertPaginationRequest,
} from '@/types/recommend';
import { externalApi } from './common/apiInstance';

// 추천 강의 조회
export const getRecommendations = async (
  contentSeq: number,
  request: InfiniteScrollRequest,
) => {
  const { data } = await externalApi.get<SnapReviewListResponse>(
    `/learning/contents/${contentSeq}/recommendations`,
    {
      params: request,
    },
  );
  return data;
};

// 전문가 추천 조회
export const getExpertRecommendations = async (
  contentSeq: number,
  request: ExpertPaginationRequest,
) => {
  const { data } = await externalApi.get<ExpertListResponse>(
    `/learning/contents/${contentSeq}/expert-recommendations`,
    {
      params: request,
    },
  );
  return data;
};
