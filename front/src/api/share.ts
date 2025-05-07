import { ShareResponse, ShareCheck } from '@/types/share';
import { externalApi } from './common/apiInstance';

// 스냅 리뷰 공유 여부 조회
export const getShareCheck = async (contentSeq: number) => {
  const { data } = await externalApi.get<{ data: ShareCheck | null }>(
    `/learning/contents/${contentSeq}/share-code`,
  );
  return {
    hasShareCode: data !== null,
    data: data,
  };
};

// 공유 코드 생성
export const createShareCode = async (contentSeq: number) => {
  const { data } = await externalApi.post<{ data: ShareCheck }, never>(
    `/learning/contents/${contentSeq}/snap-review/share`,
  );
  return data.data;
};

// 공유 코드 확인 및 생성 (없으면 생성)
export const checkAndCreateShareCode = async (contentSeq: number) => {
  const { hasShareCode, data } = await getShareCheck(contentSeq);
  if (!hasShareCode) {
    return await createShareCode(contentSeq);
  }
  return data;
};

// 공유 코드로 스냅 리뷰 조회
export const getShareReview = async (shareCode: string) => {
  const { data } = await externalApi.get<{ data: ShareResponse }>(
    `/learning/share/${shareCode}`,
  );
  return data.data;
};

// 스냅 리뷰 공유 취소
export const deleteShareLink = async (shareCode: string) => {
  await externalApi.delete<{ data: null }>(`/learning/share/${shareCode}`);
};
