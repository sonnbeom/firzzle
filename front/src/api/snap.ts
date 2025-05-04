import { DateGroup, Review, FrameDescriptions } from '@/types/snapReview';
import { externalApi } from './common/apiInstance';

// 스냅북 리뷰 목록 조회
export const getSnapReviews = () => {
  return externalApi.get<DateGroup[]>('/snap-reviews');
};

// 콘텐츠별 스냅 리뷰 조회
export const getContentSnapReviews = (contentId: string) => {
  return externalApi.get<Review>(`/contents/${contentId}/snap-review`);
};

// 프레임 설명 조회
export const getFrameDescriptions = (uuid: string, contentId: string) => {
  return externalApi.get<FrameDescriptions>(`/users/${uuid}/contents/${contentId}/snap-review/notes`);
};    