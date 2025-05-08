// 공통 응답 타입
interface ApiResponse {
  status: 'OK' | 'FAIL';
  cause: string;
  message: string;
  prevUrl: string;
  redirectUrl: string;
}
export type ApiResponseWithoutData = ApiResponse & {
  data: null;
};

export type ApiResponseWithData<T> = ApiResponse & {
  data: T;
};

export type ApiResponseError = ApiResponse & {
  data: null;
};

// 무한스크롤 응답 타입
export interface InfiniteScrollResponse<T> {
  content: T[];
  p_pageno: number;
  p_pagesize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  hasNext: boolean;
}

// 무한스크롤 요청 타입
export interface InfiniteScrollRequest {
  p_pageno?: number;
  p_pagesize?: number;
  p_order?: string;
  p_sortorder?: string;
  keyword?: string;
  category?: string;
  status?: string;
}
