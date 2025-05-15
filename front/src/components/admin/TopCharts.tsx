'use client';

import CurveGraphCard from '@/components/admin/CurveGraphCard';
import { TransitionsResponse, DataSet } from '@/types/chart';

interface TopChartsProps {
  loginRateData: TransitionsResponse | null;
  educationStartData: TransitionsResponse | null;
  functionChangeData: TransitionsResponse | null;
}

const getLoginRateData = (data: TransitionsResponse): DataSet[] => [
  {
    label: '',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions.VISIT ? (transitions.LOGIN / transitions.VISIT) * 100 : 0,
    })),
  },
];

const getEducationData = (data: TransitionsResponse): DataSet[] => [
  {
    label: '',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions.CONTENT_CREATED
        ? (transitions.START_LEARNING / transitions.CONTENT_CREATED) * 100
        : 0,
    })),
  },
];

const getFunctionChangeData = (data: TransitionsResponse): DataSet[] => [
  {
    label: 'SUMMARY > QUIZ',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions['SUMMARY=>QUIZ_READ'] || 0,
    })),
  },
  {
    label: 'QUIZ > SNAP_REVIEW',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions['QUIZ_READ=>SNAP_REVIEW_READ'] || 0,
    })),
  },
  {
    label: 'SNAP_REVIEW > RECOMMEND',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions['SNAP_REVIEW_READ=>RECOMMEND'] || 0,
    })),
  },
];

const TopCharts = ({
  loginRateData,
  educationStartData,
  functionChangeData,
}: TopChartsProps) => {
  return (
    <div className='flex flex-col gap-6'>
      {loginRateData && (
        <CurveGraphCard
          title='로그인 완료율'
          description='전체 방문자 수 대비 로그인 완료한 사용자 수'
          dataSets={getLoginRateData(loginRateData)}
        />
      )}

      {educationStartData && (
        <CurveGraphCard
          title='학습 시작률'
          description='영상 링크 입력한 사용자 수 대비 학습 시작한 사용자 수'
          dataSets={getEducationData(educationStartData)}
        />
      )}

      {functionChangeData && (
        <CurveGraphCard
          title='기능 전환율'
          description='각 기능 사용자 수 대비 다음 플로우로 전환한 사용자 수'
          dataSets={getFunctionChangeData(functionChangeData)}
        />
      )}
    </div>
  );
};

export default TopCharts;
