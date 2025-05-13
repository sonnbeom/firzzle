'use client';

import { useState } from 'react';

import CurveGraphCard from '@/components/admin/CurveGraphCard';
import DateRangeSelector from '@/components/admin/DateRangeSelector';
import BasicDropDown from '@/components/common/BasicDropDown';
import { DateRangeData } from '@/types/chart';

const FEATURE_OPTIONS = [
  { label: '요약노트', value: 'summary' },
  { label: '러닝챗', value: 'chat' },
  { label: 'AI퀴즈', value: 'quiz' },
  { label: '스냅리뷰', value: 'snap' },
  { label: '관련링크', value: 'link' },
  { label: '학습 내역', value: 'history' },
];

const MOCK_DATA = {
  summary: [65, 22, 37, 25, 45, 12, 52, 12, 53, 48, 37],
  chat: [45, 32, 47, 35, 55, 22, 42, 22, 43, 38, 27],
  quiz: [55, 42, 57, 45, 65, 32, 52, 32, 53, 48, 37],
  snap: [35, 22, 27, 15, 35, 12, 32, 12, 33, 28, 17],
  link: [75, 62, 77, 65, 85, 52, 72, 52, 73, 68, 57],
  history: [85, 72, 87, 75, 95, 62, 82, 62, 83, 78, 67],
};

interface ChartData {
  label: string;
  data: { x: string; y: number }[];
}

const StrategyBoardPage = () => {
  const [startDate, setStartDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(new Date());

  const handleDateChange = ({
    startDate: newStart,
    endDate: newEnd,
    formattedStart,
    formattedEnd,
  }: DateRangeData) => {
    setStartDate(newStart);
    setEndDate(newEnd);
    console.log('Formatted dates:', formattedStart, formattedEnd);
  };

  const generateDates = (startDate: Date, days: number) => {
    const dates = [];
    for (let i = 0; i < days; i++) {
      const date = new Date(startDate);
      date.setDate(date.getDate() + i);
      dates.push(
        date.toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' }),
      );
    }
    return dates;
  };

  const dates = generateDates(startDate, 30);

  const graphData = [
    {
      title: '로그인 완료율',
      description: '전체 방문자 수 대비 로그인 완료한 사용자 수',
      dataSets: [
        {
          label: '로그인 완료율',
          data: [68, 20, 32, 20, 40, 12, 57, 17, 58, 53, 42].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
      ],
      startDate,
      endDate,
    },
    {
      title: '학습 시작률',
      description: '영상 링크 입력한 사용자 수 대비 학습 시작한 사용자 수',
      dataSets: [
        {
          label: '학습 시작률',
          data: [67, 18, 32, 20, 40, 15, 57, 17, 55, 52, 41].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
      ],
      startDate,
      endDate,
    },
    {
      title: '기능 전환률',
      description: '각 기능 사용자 수 대비 다음 플로우로 전환한 사용자 수',
      dataSets: [
        {
          label: '요약노트 → 퀴즈',
          data: [69, 21, 34, 20, 15, 57, 15, 57, 52, 54, 42].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
        {
          label: '퀴즈 → 스냅리뷰',
          data: [42, 50, 35, 82, 15, 57, 36, 82, 94, 15, 15].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
        {
          label: '스냅리뷰 → 추천',
          data: [42, 50, 35, 70, 93, 40, 40, 94, 28, 87, 15].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
      ],
      startDate,
      endDate,
    },
  ];

  return (
    <div className='flex flex-col gap-6 p-6'>
      <DateRangeSelector
        onChange={handleDateChange}
        initialStartDate={startDate}
        initialEndDate={endDate}
      />
      {graphData.map((cardProps, index) => (
        <CurveGraphCard key={index} {...cardProps} />
      ))}
      <div className='flex flex-col gap-4'>
        <div className='flex items-center gap-3 px-4'>
          <h2 className='text-md ml-2 font-semibold'>기능 사용률</h2>
          <BasicDropDown
            items={FEATURE_OPTIONS}
            defaultValue='summary'
            onChange={(value) => console.log('Selected:', value)}
          />
        </div>
        <CurveGraphCard
          dataSets={[
            {
              label: '사용률',
              data: MOCK_DATA.summary.map((y, i) => ({
                x: dates[i],
                y,
              })),
            },
          ]}
          startDate={startDate}
          endDate={endDate}
        />
      </div>
    </div>
  );
};

export default StrategyBoardPage;
