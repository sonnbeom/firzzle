'use client';

import CurveGraphCard from '@/components/admin/CurveGraphCard';
import { TransitionsResponse } from '@/types/chart';
import { convertTransitionsToDataSets, chartMappings } from '@/utils/chart';

interface TopChartsProps {
  loginRateData: TransitionsResponse | null;
  educationStartData: TransitionsResponse | null;
  functionChangeData: TransitionsResponse | null;
}

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
          dataSets={convertTransitionsToDataSets(
            loginRateData,
            chartMappings.loginRate,
          )}
        />
      )}

      {educationStartData && (
        <CurveGraphCard
          title='학습 시작률'
          description='영상 링크 입력한 사용자 수 대비 학습 시작한 사용자 수'
          dataSets={convertTransitionsToDataSets(
            educationStartData,
            chartMappings.educationRate,
          )}
        />
      )}

      {functionChangeData && (
        <CurveGraphCard
          title='기능 전환율'
          description='각 기능 사용자 수 대비 다음 플로우로 전환한 사용자 수'
          dataSets={convertTransitionsToDataSets(
            functionChangeData,
            chartMappings.functionChange,
          )}
        />
      )}
    </div>
  );
};

export default TopCharts;
