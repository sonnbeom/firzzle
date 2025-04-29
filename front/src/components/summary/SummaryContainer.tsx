'use client';

import { useState } from 'react';
import { SummaryData, SummaryLevel } from 'types/summary';
import LevelTab from './LevelTab';
import SummaryContent from './SummaryContent';

interface SummaryContainerProps {
  easyData: SummaryData[];
  highData: SummaryData[];
}

const SummaryContainer = ({ easyData, highData }: SummaryContainerProps) => {
  const [level, setLevel] = useState<SummaryLevel>('Easy');

  return (
    <div className='flex w-full flex-col gap-7'>
      {/* 요약 수준 탭 */}
      <LevelTab level={level} setLevel={setLevel} />
      {/* 요약 내용 */}
      <SummaryContent level={level} easyData={easyData} highData={highData} />
    </div>
  );
};

export default SummaryContainer;
