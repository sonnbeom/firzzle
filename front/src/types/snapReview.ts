// 날짜별 목록 리스트
export interface DateGroup {
  date: string;
  items: SnapItem[];
}

// 개별 스냅 리뷰
export interface SnapItem {
  id: string;
  title: string;
  youtubeUrl: string;
  date: string;
  length: number;
}

// 리뷰 상세
export interface Review {
  title: string;
  date: string;
  images: Frame[];
}

// 리뷰별 개별 프레임 정보보
export interface Frame {
  id: string;
  src: string;
  timestamp: number;
}

// 전체 프레임 정보보
export interface FrameDescriptions {
  notes: FrameDescription[];
}

// 프레임 설명
export interface FrameDescription {
  frameId: string;
  description: string | null;
}