import { InfiniteScrollResponse } from './common';

export interface Content {
  contentSeq: string;
  title: string;
  description: string;
  contentType: string;
  videoId: string;
  url: string;
  thumbnailUrl: string;
  duration: number; // 영상 길이 (초)
  tags: string;
  processStatus: 'Q' | 'P' | 'C' | 'F'; // 영상 분석 처리 단계 (Q: 대기중, P: 처리중, C: 완료, F: 실패)
  analysisData: string;
  transcript: string;
  indate: string;
  completedAt: string;
  deleteYn: string;
  formattedDuration: string; // HH:MM:SS로 변환된 영상 길이
  processStatusText: string; // 영상 분석 처리 단계 텍스트
}

// 학습 컨텐츠 조회 응답 타입
export type ContentResponse = Content;

// 학습 컨텐츠 목록 조회 응답 타입
export type ContentListResponse = InfiniteScrollResponse<Content>;
