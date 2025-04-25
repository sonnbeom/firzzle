import TimeStamp from './TimeStamp';

interface SummaryCardProps {
  title: string;
  description: string;
  time: string;
}

const SummaryCard = ({ title, description, time }: SummaryCardProps) => {
  return (
    <div className='flex w-full flex-col gap-7'>
      <div className='flex w-full flex-col items-start gap-2'>
        {/* 타이틀과 타임스탬프 */}
        <div className='flex items-end gap-3'>
          <p className='text-xl font-semibold break-words whitespace-pre-wrap text-gray-950'>
            {title}
          </p>
          <TimeStamp time={time} />
        </div>
        {/* 상세 요약 */}
        <p className='text-lg break-words whitespace-pre-wrap text-gray-950'>
          {description}
        </p>
      </div>
      {/* 구분선 */}
      <hr className='w-full border border-gray-100' />
    </div>
  );
};

export default SummaryCard;
