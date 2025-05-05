import { ContentResponse } from '@/types/content';
import { externalApi } from './common/apiInstance';

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
