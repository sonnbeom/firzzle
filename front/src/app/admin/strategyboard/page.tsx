'use client';

import { useState, useEffect, useCallback } from 'react';
import BottomChart from '@/components/admin/BottomChart';
import DateRangeSelector from '@/components/admin/DateRangeSelector';
import TopCharts from '@/components/admin/TopCharts';
import useStrategyData from '@/hooks/useStrategyData';
import { DateRangeData } from '@/types/chart';

const StrategyBoardPage = () => {
  const today = new Date();
  const weekAgo = new Date(today);
  weekAgo.setDate(today.getDate() - 7);

  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  const [startDate, setStartDate] = useState<Date>(weekAgo);
  const [endDate, setEndDate] = useState<Date>(today);
  const [formattedStart, setFormattedStart] = useState<string>(
    formatDate(weekAgo),
  );
  const [formattedEnd, setFormattedEnd] = useState<string>(formatDate(today));
  const [selectedOption, setSelectedOption] = useState<string>('요약노트');

  const {
    data: {
      loginRateData,
      educationStartData,
      functionChangeData,
      selectedChartData,
    },
    actions: { fetchData, fetchSelectedData },
    isLoading,
  } = useStrategyData();

  const handleDateChange = useCallback(
    async ({
      startDate: newStart,
      endDate: newEnd,
      formattedStart,
      formattedEnd,
    }: DateRangeData) => {
      if (isLoading) return;

      setStartDate(newStart);
      setEndDate(newEnd);
      setFormattedStart(formattedStart);
      setFormattedEnd(formattedEnd);
    },
    [isLoading],
  );

  const handleOptionChange = useCallback((value: string) => {
    setSelectedOption(value);
  }, []);

  // 날짜, 옵션 변경 시 데이터 불러오기
  useEffect(() => {
    if (!isLoading && formattedStart && formattedEnd) {
      fetchData(formattedStart, formattedEnd);
    }
  }, [formattedStart, formattedEnd]);

  useEffect(() => {
    if (!isLoading && formattedStart && formattedEnd && selectedOption) {
      fetchSelectedData(selectedOption, formattedStart, formattedEnd);
    }
  }, [selectedOption, formattedStart, formattedEnd]);

  return (
    <div className='flex flex-col gap-6 bg-blue-50 p-6'>
      <DateRangeSelector
        onChange={handleDateChange}
        initialStartDate={startDate}
        initialEndDate={endDate}
      />

      <TopCharts
        loginRateData={loginRateData}
        educationStartData={educationStartData}
        functionChangeData={functionChangeData}
      />

      <BottomChart
        selectedOption={selectedOption}
        selectedChartData={selectedChartData}
        onOptionChange={handleOptionChange}
      />
    </div>
  );
};

export default StrategyBoardPage;
