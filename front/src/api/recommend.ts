import { InfiniteScrollRequest } from '@/types/common';
import {
  SnapReviewListResponse,
  ExpertListResponse,
  ExpertPaginationRequest,
} from '@/types/recommend';

// 추천 강의 조회
export const getRecommendations = async (
  contentSeq: string,
  request: InfiniteScrollRequest,
): Promise<SnapReviewListResponse> => {
  const params = new URLSearchParams();
  
  // 선택적 파라미터들을 URLSearchParams에 추가
  if (request.p_pageno) params.append('p_pageno', request.p_pageno.toString());
  if (request.p_pagesize) params.append('p_pagesize', request.p_pagesize.toString());
  if (request.p_order) params.append('p_order', request.p_order);
  if (request.p_sortorder) params.append('p_sortorder', request.p_sortorder);
  if (request.keyword) params.append('keyword', request.keyword);
  if (request.category) params.append('category', request.category);
  if (request.status) params.append('status', request.status);

  const response = await fetch(
    `/api/learning/contents/${contentSeq}/recommendations?${params}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 전문가 추천 조회
export const getExpertRecommendations = async (
  contentSeq: string,
  request: ExpertPaginationRequest,
): Promise<ExpertListResponse> => {
  const params = new URLSearchParams();
  
  // 선택적 파라미터들을 URLSearchParams에 추가
  if (request.p_pageno) params.append('p_pageno', request.p_pageno.toString());
  if (request.p_pagesize) params.append('p_pagesize', request.p_pagesize.toString());

  params.append('type', 'expert');
  const response = await fetch(
    `/api/learning/contents/${contentSeq}/recommendations?${params}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};
