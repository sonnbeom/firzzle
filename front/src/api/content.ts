import { InfiniteScrollRequest } from '@/types/common';
import { ContentRegisterRequest, ContentResponse } from '@/types/content';
import { ContentListResponse } from '@/types/content';
import { api } from './common/apiInstance';

// 학습 컨텐츠 분석
export const postContent = async (request: ContentRegisterRequest) => {
  const response = await fetch('/api/learning/contents', {
    method: 'POST',
    body: JSON.stringify(request),
  });

  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.data;
};

// 학습 컨텐츠 정보 조회
export const getContent = async (contentSeq: string) => {
  const { data } = await api.get<ContentResponse>(
    `/learning/contents/${contentSeq}`,
  );
  return data;
};

// 학습 컨텐츠 목록 조회
export const getContentList = async (request: InfiniteScrollRequest) => {
  const { data } = await api.get<ContentListResponse>(`/learning/contents`, {
    params: request,
  });
  return data;
};
