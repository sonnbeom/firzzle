import { ShareResponse, ShareCheck } from "@/types/share";
import { externalApi } from './common/apiInstance';

// 스냅 리뷰 공유 여부 조회
export const getShareCheck = async (contentSeq: number) => {
    const response = await externalApi.get<ShareCheck>(`/learning/contents/${contentSeq}/share-code`);
    return {
        hasShareCode: response.data !== null,
        data: response.data
    };
}

// 공유 코드 생성
export const createShareCode = async (contentSeq: number) => {
    return externalApi.post<ShareResponse, {}>(`/learning/contents/${contentSeq}/snap-review/share`);
}

// 공유 코드로 스냅 리뷰 조회
export const getShareReview = async (shareCode: string) => {
    return externalApi.get<ShareResponse>(`/learning/share/${shareCode}`);
}

// 스냅 리뷰 공유 취소
export const deleteShareLink = async (shareCode: string) => {
    return externalApi.delete<{}>(`/learning/share/${shareCode}`);
}