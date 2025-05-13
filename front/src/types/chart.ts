export interface TransitionData {
  date: string;
  transitions: {
    [key: string]: number;
  };
}

// 날짜 범위 및 변환
export interface DateRangeData {
  startDate: Date;
  endDate: Date;
  formattedStart: string;
  formattedEnd: string;
}

export interface DateRangeSelectorProps {
  onChange?: (dates: DateRangeData) => void;
  initialStartDate?: Date;
  initialEndDate?: Date;
}

export type TransitionsResponse = TransitionData[];

// 차트 활용 데이터
export interface DataPoint {
  x: string; // x축 날짜
  y: number; // y축 값
}

export interface DataSet {
  label: string;
  data: DataPoint[];
}

// 차트 주입 데이터
export interface CurveGraphCardProps {
  title?: string;
  description?: string;
  tags?: {
    text: string;
    color: string;
  }[];
  dataSets: DataSet[];
  mode?: {
    text: string;
  };
}
