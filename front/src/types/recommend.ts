import { InfiniteScrollResponse } from './common';

// 추천 영상 조회 타입
export type SnapReviewListResponse = InfiniteScrollResponse<{
  content: Content[];
  originTags: string;
}>
// 추천 영상 컨텐츠 타입
export interface Content {
  contentSeq: number;
  title: string;
  description: string;
  contentType: string;
  videoId: string;
  url: string;
  thumbnailUrl: string;
  duration: number;
  tags: string;
  processStatus: string;
  analysisData: null;
  transcript: null;
  indate: string;
  completedAt: string;
  deleteYn: string;
  processStatusText: string;
  formattedDuration: string;
}


// 추천 강의 활용 데이터 타입
export interface RecommendVideo {
  title: string;
  url: string;
  thumbnailUrl: string;
}
// 추천 강의 Props 타입
export interface VideoProps {
  videos: RecommendVideo[];
  originTags : string;
}

// 전문가 추천 데이터 타입
export interface ExpertRecommend {
  name: string;
  description: string;
  thumbnail: string;
  url: string;
  keyword: string;
}
