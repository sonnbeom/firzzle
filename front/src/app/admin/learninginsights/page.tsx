'use client';

import { useState } from 'react';

import CurveGraphCard from '@/components/admin/CurveGraphCard';
import DateRangeSelector from '@/components/admin/DateRangeSelector';

interface Tag {
  text: string;
  color: string;
}

interface ChartData {
  label: string;
  data: { x: string; y: number }[];
}

interface InsightCardProps {
  title: string;
  description: string;
  tags: Tag[];
  dataSets: ChartData[];
  startDate: Date;
  endDate: Date;
}

const LearnigInsightPage = () => {
  const [startDate, setStartDate] = useState<Date>(new Date('2024-04-19'));
  const [endDate, setEndDate] = useState<Date>(new Date('2024-05-18'));

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

  const handleDateChange = ({
    startDate: newStart,
    endDate: newEnd,
  }: {
    startDate: Date;
    endDate: Date;
  }) => {
    setStartDate(newStart);
    setEndDate(newEnd);
  };

  const dates = generateDates(startDate, 30);

  const insightCards: InsightCardProps[] = [
    {
      title: '요약 단계별 클릭 수',
      description: '요약 단계(High/Easy) 별 클릭 비율',
      tags: [{ text: '요약 노트', color: '#FFB800' }],
      dataSets: [
        {
          label: 'High',
          data: [69, 42, 17, 35, 20, 41, 15, 52, 15, 57, 52, 92, 42].map(
            (y, i) => ({ x: dates[i], y }),
          ),
        },
        {
          label: 'Easy',
          data: [42, 50, 35, 82, 15, 15, 36, 94, 94, 54, 15].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
      ],
      startDate,
      endDate,
    },
    {
      title: '문제 정답률',
      description: '퀴즈 문제 수 대비 AI퀴즈 문제 정답률',
      tags: [
        { text: 'AI 퀴즈', color: '#4CAF50' },
        { text: '런닝챗', color: '#2196F3' },
      ],
      dataSets: [
        {
          label: 'AI 퀴즈',
          data: [68, 18, 35, 41, 12, 58, 18, 58, 53, 90, 42].map((y, i) => ({
            x: dates[i],
            y,
          })),
        },
      ],
      startDate,
      endDate,
    },
    {
      title: '작성 완료율',
      description:
        '스냅리뷰 사용자 수 대비 모든 스냅에 대한 설명을 입력 완료한 수',
      tags: [{ text: '스냅리뷰', color: '#9C27B0' }],
      dataSets: [
        {
          label: '스냅리뷰',
          data: [67, 18, 32, 20, 40, 57, 17, 55, 52, 91, 41].map((y, i) => ({
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
      <DateRangeSelector onChange={handleDateChange} />
      {insightCards.map((cardProps, index) => (
        <CurveGraphCard key={index} {...cardProps} />
      ))}
    </div>
  );
};

export default LearnigInsightPage;
