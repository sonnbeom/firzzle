export interface TransitionData {
  date: string;
  transitions: {
    [key: string]: number;
  };
}

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
