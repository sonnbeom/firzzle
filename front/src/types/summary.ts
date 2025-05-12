export type SummaryLevel = 'Easy' | 'High';

export interface SummaryDetail {
  sectionSeq: string;
  title: string;
  startTime: number;
  details: string;
}

export interface SummaryResponse {
  contentSeq: string;
  easySummarySeq: string;
  easySections: SummaryDetail[];
  easyIndate: string;
  hardSummarySeq: string;
  hardSections: SummaryDetail[];
  hardIndate: string;
}
