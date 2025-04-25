'use client';

import LevelButton from '@/components/summary/LevelButton';
import SummaryContent from '@/components/summary/SummaryContent';

const Summary = () => {
  return (
    <div className='flex w-full flex-col gap-7'>
      {/* 요약 수준 탭 */}
      <div className='flex w-full gap-2'>
        <LevelButton
          isActive={true}
          title='쉽게 설명해주세요'
          onClick={() => {}}
        />
        <LevelButton title='배경 지식이 있어요' onClick={() => {}} />
      </div>
      {/* 요약 내용 */}
      <SummaryContent />
    </div>
  );
};

export default Summary;
