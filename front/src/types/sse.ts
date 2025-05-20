export interface SSEEventData {
  message?: string;
  taskId?: string;
  contentSeq?: string;
  timestamp?: number;
  timePoints?: string[];
  currentTime?: string;
  currentIndex?: number;
  totalTopics?: number;
  blockCount?: number;
  blocks?: Array<{
    title: string;
    time: string;
  }>;
}
