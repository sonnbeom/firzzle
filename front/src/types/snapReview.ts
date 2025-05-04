export interface SnapItem {
  id: string;
  title: string;
  youtubeUrl: string;  // YouTube video URL
  date: string;
  length: number;
}

export interface DateGroup {
  date: string;
  items: SnapItem[];
}

export interface Frame {
  src: string;
  description: string;
  timestamp: number;
}

export interface Review {
  title: string;
  date: string;
  images: Frame[];
}
