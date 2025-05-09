import { InfiniteScrollResponse, InfiniteScrollRequest } from './common';
import { Content } from './content';

// 추천 영상 조회 타입
export interface SnapReviewListResponse
  extends InfiniteScrollResponse<Content> {
  originTags?: string;
}

// 추천 강의 Props 타입
export type VideoProps = Pick<Content, 'title' | 'url' | 'thumbnailUrl'>;

// 전문가 추천 응답 타입
export interface ExpertListResponse extends InfiniteScrollResponse<Expert> {
  originTags?: string;
}

// 전문가 데이터 타입입
export interface Expert {
  expertSeq: number;
  name: string;
  title: string;
  company: string;
  profileImageUrl: string;
  linkedinUrl: string;
  relevance: number;
  expertise: string[];
}

export type ExpertPaginationRequest = Pick<
  InfiniteScrollRequest,
  'p_pageno' | 'p_pagesize'
>;
