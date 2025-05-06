import { SnapReview, SnapReviewListResponse, UpdateFrameCommentsRequest, UpdateFrameCommentsResponse } from '@/types/snapReview';
import { externalApi } from './common/apiInstance';

// 스냅리뷰 목록 조회
export const getSnapReviews = () => {
  return externalApi.get<SnapReviewListResponse>('/learning/snap-reviews');
};

// 콘텐츠별 스냅리뷰 조회
export const getContentSnapReview = (contentSeq: number) => {
  return externalApi.get<{ data: SnapReview }>(`/learning/contents/${contentSeq}/snap-review`);
};

// 프레임 설명 수정
export const updateFrameComments = (contentSeq: number, request: UpdateFrameCommentsRequest) => {
  return externalApi.patch<{ data: UpdateFrameCommentsResponse[] }, UpdateFrameCommentsRequest>(
    `/learning/contents/${contentSeq}/snap-review`,
    { body: request }
  );
};