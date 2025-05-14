'use client';

import CurveGraphCard from '@/components/admin/CurveGraphCard';
import BasicDropDown from '@/components/common/BasicDropDown';
import { TransitionsResponse, DataSet } from '@/types/chart';

interface BottomChartProps {
  selectedOption: string;
  selectedChartData: TransitionsResponse | null;
  onOptionChange: (value: string) => void;
}

const getSummaryNoteData = (data: TransitionsResponse): DataSet[] => [
  {
    label: '',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions.DIFFICULT || 0,
    })),
  },
  {
    label: '',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions.EASY || 0,
    })),
  },
];

const getSnapReviewData = (data: TransitionsResponse): DataSet[] => [
  {
    label: '',
    data: data.map(({ date, transitions }) => ({
      x: date,
      y: transitions.START_LEARNING
        ? (transitions.SNAP_REVIEW_INPUT / transitions.START_LEARNING) * 100
        : 0,
    })),
  },
];

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
              ? getSummaryNoteData(selectedChartData)
              : getSnapReviewData(selectedChartData)
          }
        />
      )}
    </div>
  );
};

export default BottomChart;
