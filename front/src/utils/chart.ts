import { TransitionData, DataSet } from '@/types/chart';

export const convertTransitionsToDataSets = (
  transitionData: TransitionData[],
): DataSet[] => {
  const allKeys = new Set<string>();
  transitionData.forEach((data) => {
    Object.keys(data.transitions).forEach((key) => allKeys.add(key));
  });

  return Array.from(allKeys).map((key) => ({
    label: key,
    data: transitionData.map((data) => ({
      x: data.date,
      y: data.transitions[key] || 0,
    })),
  }));
};
