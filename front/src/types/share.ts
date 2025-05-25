import { Frame } from './snapReview';

// 공유된 스냅 리뷰 정보
export interface ShareResponse {
  contentSeq: number;
  contentTitle: string;
  thumbnailUrl: string;
  indate: string;
  frames: Frame[];
}

// 공유 코드 정보
export interface ShareCheck {
  shareCode: string;
  contentSeq: number;
  shareUrl: string;
  indate: string;
}
