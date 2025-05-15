import { ShareResponse } from '@/types/share';
import { api } from './common/apiInstance';

// 스냅 리뷰 공유 여부 조회
export const getShareCheck = async (contentSeq: string) => {
  const response = await fetch(
    `/api/learning/contents/${contentSeq}/share-code`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return {
    hasShareCode: data.data !== null,
    data: data.data,
  };
};

// 공유 코드 생성
export const createShareCode = async (contentSeq: string) => {
  const response = await fetch(
    `/api/learning/contents/${contentSeq}/share-code`,
    {
      method: 'POST',
    },
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data.data;
};

// 공유 코드 확인 및 생성 (없으면 생성)
export const checkAndCreateShareCode = async (contentSeq: string) => {
  const { hasShareCode, data } = await getShareCheck(contentSeq);
  if (!hasShareCode) {
    return await createShareCode(contentSeq);
  }
  return data;
};

// 공유 코드로 스냅 리뷰 조회

export const getShareReview = async (
  shareCode: string,
): Promise<ShareResponse> => {
  const { data } = await api.get<ShareResponse>(`/learning/share/${shareCode}`);
  return data;
};

// 스냅 리뷰 공유 취소
export const deleteShareLink = async (shareCode: string) => {
  await api.delete<{ data: null }>(`/learning/share/${shareCode}`);
};
