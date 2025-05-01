import { SummaryData, SummaryLevel } from '@/types/summary';
import SelectableText from './SelectableText';
import SummaryCard from './SummaryCard';

interface SummaryContentProps {
  level: SummaryLevel;
  easyData: SummaryData[];
  highData: SummaryData[];
}

const SummaryContent = async ({
  level,
  easyData,
  highData,
}: SummaryContentProps) => {
  const summaryData = level === 'Easy' ? easyData : highData;

  return (
    <div className='flex w-full flex-col gap-7 px-2'>
      <SelectableText>
        {summaryData.map((item) => (
          <SummaryCard
            key={item.id}
            title={item.title}
            description={item.description}
            time={item.time}
          />
        ))}
      </SelectableText>
    </div>
  );
};

export default SummaryContent;
