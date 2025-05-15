import { getSummary } from '@/api/summary';
import LevelTab from '@/components/summary/LevelTab';
import SummaryContent from '@/components/summary/SummaryContent';
import { SummaryLevel } from '@/types/summary';

interface PageProps {
  params: Promise<{ id: string }>;
  searchParams: Promise<{
    tab?: SummaryLevel;
  }>;
}

const Summary = async ({ params, searchParams }: PageProps) => {
  const { id } = await params;
  const summaryData = await getSummary(id);
  const tabParams = await searchParams;
  const tab = tabParams.tab || 'Easy';
  const data = {
    easyData: ([] = summaryData.easySections),
    highData: ([] = summaryData.hardSections),
  };

  return (
    <div className='flex w-full flex-col gap-4 lg:gap-7'>
      <LevelTab initialTab={tab} />
      <SummaryContent
        level={tab}
        easyData={data.easyData}
        highData={data.highData}
      />
    </div>
  );
};

export default Summary;
