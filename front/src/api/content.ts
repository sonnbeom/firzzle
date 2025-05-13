import { InfiniteScrollRequest } from '@/types/common';
import { ContentListResponse, ContentResponse } from '@/types/content';
import { externalApi } from './common/apiInstance';

// 학습 컨텐츠 분석
export const postContent = async () => {
  return await externalApi.post('/learning/contents');
};

// 학습 컨텐츠 정보 조회
export const getContent = async (contentSeq: string) => {
  const { data } = await externalApi.get<ContentResponse>(
    `/learning/contents/${contentSeq}`,
  );
  return data;
};

// 학습 컨텐츠 목록 조회
export const getContentList = async (request: InfiniteScrollRequest) => {
  const { data } = await externalApi.get<ContentListResponse>(
    `/learning/contents`,
    {
      params: request,
    },
  );
  return data;
};
