import { InfiniteScrollResponse } from './common';

// 스냅리뷰 목록 조회 응답
export type SnapReviewListResponse = InfiniteScrollResponse<{
  dailySnapReviews: {
    [date: string]: SnapReview[];
  };
  totalDays: number;
}>;

// 스냅리뷰 정보
export interface SnapReview {
  contentSeq: number;
  contentTitle: string;
  category?: string;
  thumbnailUrl: string;
  representativeImageUrl?: string;
  indate: string;
  frameCount?: number;
  frames?: Frame[];
}

// 프레임 정보
export interface Frame {
  frameSeq: number;
  imageUrl: string;
  timestamp: number;
  formattedTimestamp: string;
  comment: string | null;
}

// 프레임 설명 수정 요청
export interface UpdateFrameCommentsRequest {
  frames: {
    frameSeq: number;
    comment: string;
  }[];
}

// 프레임 설명 수정 응답
export interface UpdateFrameCommentsResponse {
  frameSeq: number;
  contentSeq: number;
  imageUrl: string;
  timestamp: number;
  formattedTimestamp: string;
  comment: string | null;
  indate: string;
  ldate: string;
}
