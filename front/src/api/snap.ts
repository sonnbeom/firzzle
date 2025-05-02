import { DateGroup } from '@/types/snapReview';
import { externalApi } from './common/apiInstance';

// 스냅북 리뷰 목록 조회
export const getSnapReviews = () => {
  return externalApi.get<DateGroup[]>('/snap-reviews');
};
