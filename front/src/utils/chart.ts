import { TransitionData, DataSet } from '@/types/chart';

type TransitionKey = string;
type TransitionLabel = string;
type TransitionMapping = {
  key: TransitionKey;
  label: TransitionLabel;
  calculateValue?: (transitions: Record<string, number>) => number;
};

export const convertTransitionsToDataSets = (
  transitionData: TransitionData[],
  mappings: TransitionMapping[],
): DataSet[] => {
  return mappings.map(({ key, label, calculateValue }) => ({
    label,
    data: transitionData.map(({ date, transitions }) => ({
      x: date,
      y: calculateValue ? calculateValue(transitions) : transitions[key] || 0,
    })),
  }));
};

export const chartMappings = {
  // 로그인 완료율
  loginRate: [
    {
      key: 'LOGIN',
      label: '',
      calculateValue: (transitions) =>
        transitions.VISIT ? (transitions.LOGIN / transitions.VISIT) * 100 : 0,
    },
  ],

  // 학습 시작률
  educationRate: [
    {
      key: 'START_LEARNING',
      label: '',
      calculateValue: (transitions) =>
        transitions.CONTENT_CREATED
          ? (transitions.START_LEARNING / transitions.CONTENT_CREATED) * 100
          : 0,
    },
  ],

  // 기능 전환율
  functionChange: [
    {
      key: 'SUMMARY=\u003EQUIZ',
      label: 'SUMMARY > QUIZ',
    },
    {
      key: 'QUIZ=\u003ESNAP_REVIEW',
      label: 'QUIZ > SNAP_REVIEW',
    },
    {
      key: 'SNAP_REVIEW=\u003ERECOMMEND',
      label: 'SNAP_REVIEW > RECOMMEND',
    },
  ],

  // 요약노트 데이터
  summaryNote: [
    {
      key: 'DIFFICULT',
      label: 'DIFFICULT',
    },
    {
      key: 'EASY',
      label: 'EASY',
    },
  ],

  // 스냅리뷰 입력률
  snapReviewRate: [
    {
      key: 'SNAP_REVIEW_INPUT',
      label: '',
      calculateValue: (transitions) =>
        transitions.START_LEARNING && transitions.SNAP_REVIEW_INPUT
          ? (transitions.SNAP_REVIEW_INPUT / transitions.START_LEARNING) * 100
          : 0,
    },
  ],
};
