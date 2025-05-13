export interface TransitionData {
  date: string;
  transitions: {
    [key: string]: number;
  };
}

export type TransitionsResponse = TransitionData[];
