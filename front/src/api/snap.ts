import { InfiniteScrollRequest } from '@/types/common';
import {
  SnapReview,
  SnapReviewListResponse,
  UpdateFrameCommentsRequest,
  UpdateFrameCommentsResponse,
} from '@/types/snapReview';
import { externalApi } from './common/apiInstance';

// 스냅리뷰 목록 조회
export const getSnapReviews = async (request: InfiniteScrollRequest) => {
  const { data } = await externalApi.get<SnapReviewListResponse>(
    '/learning/snap-reviews',
    {
      params: request,
    },
  );
  return data;
};

// 콘텐츠별 스냅리뷰 조회
export const getContentSnapReview = async (contentSeq: number) => {
  return externalApi.get<SnapReview>(
    `/learning/contents/${contentSeq}/snap-review`,
  );
};

// 프레임 설명 수정
export const updateFrameComments = async (
  contentSeq: number,
  request: UpdateFrameCommentsRequest,
) => {
  return externalApi.patch<
    UpdateFrameCommentsResponse[],
    UpdateFrameCommentsRequest
  >(`/learning/contents/${contentSeq}/snap-review`, { body: request });
};
