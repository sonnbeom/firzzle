'use client';

import { useState } from 'react';
import BottomChart from '@/components/admin/BottomChart';
import DateRangeSelector from '@/components/admin/DateRangeSelector';
import TopCharts from '@/components/admin/TopCharts';
import useStrategyData from '@/hooks/useStrategyData';
import { DateRangeData } from '@/types/chart';

const StrategyBoardPage = () => {
  const [startDate, setStartDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(new Date());
  const [formattedStart, setFormattedStart] = useState<string>('');
  const [formattedEnd, setFormattedEnd] = useState<string>('');
  const [selectedOption, setSelectedOption] = useState<string>('요약노트');

  const {
    data: {
      loginRateData,
      educationStartData,
      functionChangeData,
      selectedChartData,
    },
    actions: { fetchData, fetchSelectedData },
  } = useStrategyData();

  // 날짜 변경 핸들러
  const handleDateChange = async ({
    startDate: newStart,
    endDate: newEnd,
    formattedStart,
    formattedEnd,
  }: DateRangeData) => {
    setStartDate(newStart);
    setEndDate(newEnd);
    setFormattedStart(formattedStart);
    setFormattedEnd(formattedEnd);

    await fetchData(formattedStart, formattedEnd);
    await fetchSelectedData(selectedOption, formattedStart, formattedEnd);
  };

  // 드롭다운 선택 핸들러
  const handleOptionChange = async (value: string) => {
    setSelectedOption(value);
    if (formattedStart && formattedEnd) {
      await fetchSelectedData(value, formattedStart, formattedEnd);
    }
  };

  return (
    <div className='flex flex-col gap-6 p-6'>
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
