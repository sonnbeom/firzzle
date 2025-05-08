import { InfiniteScrollResponse } from './common';
import {Content} from  './content'

// 추천 영상 조회 타입
export interface SnapReviewListResponse extends InfiniteScrollResponse<Content> {
  originTags?: string;
}

// 추천 강의 Props 타입
export interface VideoProps {
  title: string;
  url: string;
  thumbnailUrl: string;
}

// 전문가 추천 데이터 타입
export interface ExpertRecommend {
  name: string;
  description: string;
  thumbnail: string;
  url: string;
  keyword: string;
}
