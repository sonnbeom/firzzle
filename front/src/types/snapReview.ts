export interface SnapItem {
  id: string;
  title: string;
  thumbnail: string;
  date: string;
  length: number;
}

export interface DateGroup {
  date: string;
  items: SnapItem[];
}
