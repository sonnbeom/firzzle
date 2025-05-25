import { InfiniteScrollRequest } from '@/types/common';
import {
  SnapReviewListResponse,
  UpdateFrameCommentsRequest,
} from '@/types/snapReview';
import { api } from './common/apiInstance';

// 스냅리뷰 목록 조회
export const getSnapReviews = async (request: InfiniteScrollRequest) => {
  const { data } = await api.get<SnapReviewListResponse>(
    '/learning/snap-reviews',
    {
      params: request,
    },
  );
  return data;
};

// 콘텐츠별 스냅리뷰 조회
export const getContentSnapReview = async (contentSeq: string) => {
  const response = await fetch(
    `/api/learning/contents/${contentSeq}/snap-review`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 프레임 설명 수정
export const updateFrameComments = async (
  contentSeq: string,
  request: UpdateFrameCommentsRequest,
) => {
  const response = await fetch(
    `/api/learning/contents/${contentSeq}/snap-review`,
    {
      method: 'PATCH',
      body: JSON.stringify(request),
    },
  );

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};
