'use client';

import CurveGraphCard from '@/components/admin/CurveGraphCard';
import BasicDropDown from '@/components/common/BasicDropDown';
import { TransitionsResponse } from '@/types/chart';
import { convertTransitionsToDataSets, chartMappings } from '@/utils/chart';

interface BottomChartProps {
  selectedOption: string;
  selectedChartData: TransitionsResponse | null;
  onOptionChange: (value: string) => void;
}

const BottomChart = ({
  selectedOption,
  selectedChartData,
  onOptionChange,
}: BottomChartProps) => {
  return (
    <div className='flex flex-col gap-4'>
      <BasicDropDown
        items={[
          { value: '요약노트', label: '요약노트' },
          { value: '스냅리뷰', label: '스냅리뷰' },
        ]}
        defaultValue={selectedOption}
        onChange={onOptionChange}
      />

      {selectedChartData && (
        <CurveGraphCard
          dataSets={
            selectedOption === '요약노트'
              ? convertTransitionsToDataSets(
                  selectedChartData,
                  chartMappings.summaryNote,
                )
              : convertTransitionsToDataSets(
                  selectedChartData,
                  chartMappings.snapReviewRate,
                )
          }
        />
      )}
    </div>
  );
};

export default BottomChart;
