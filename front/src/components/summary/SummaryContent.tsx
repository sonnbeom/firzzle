'use client';

import { SummaryData, SummaryLevel } from 'types/summary';
import SelectableText from './SelectableText';
import SummaryCard from './SummaryCard';

interface SummaryContentProps {
  level: SummaryLevel;
  easyData: SummaryData[];
  highData: SummaryData[];
}

const SummaryContent = ({ level, easyData, highData }: SummaryContentProps) => {
  return (
    <div className='flex w-full flex-col gap-7 px-2'>
      <SelectableText>
        {/* Easy 데이터 */}
        <div className={level === 'Easy' ? 'block' : 'hidden'}>
          {easyData.map((item) => (
            <SummaryCard
              key={`easy-${item.id}`}
              title={item.title}
              description={item.description}
              time={item.time}
            />
          ))}
        </div>

        {/* High 데이터 */}
        <div className={level === 'High' ? 'block' : 'hidden'}>
          {highData.map((item) => (
            <SummaryCard
              key={`high-${item.id}`}
              title={item.title}
              description={item.description}
              time={item.time}
            />
          ))}
        </div>
      </SelectableText>
    </div>
  );
};

export default SummaryContent;
