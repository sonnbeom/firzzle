import { ContentListResponse, ContentResponse } from '@/types/content';
import { externalApi, internalApi } from './common/apiInstance';

export const postContent = async () => {
  return await externalApi.post('/contents');
};

// 학습 컨텐츠 정보 조회
export const getContent = async (contentSeq: string) => {
  const { data } = await externalApi.get<ContentResponse>(
    `/learning/contents/${contentSeq}`,
  );
  return data;
};

// 학습 컨텐츠 목록 조회
export const getContentList = async (
  p_pageno?: number,
  p_pagesize?: number,
  p_order?: string,
  p_sortorder?: string,
  keyword?: string,
  category?: string,
  status?: string,
) => {
  const { data } = await internalApi.get<ContentListResponse>(
    `/learning/contents`,
    {
      params: {
        p_pageno,
        p_pagesize,
        p_order,
        p_sortorder,
        keyword,
        category,
        status,
      },
    },
  );
  return data;
};
