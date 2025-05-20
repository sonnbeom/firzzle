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
  const { tab = 'Easy' } = await searchParams;
  const summaryData = await getSummary(id);

  return (
    <div className='flex w-full flex-col gap-4 px-2 lg:gap-7'>
      <LevelTab initialTab={tab} />
      <SummaryContent
        level={tab}
        easyData={summaryData.easySections}
        highData={summaryData.hardSections}
      />
    </div>
  );
};

export default Summary;
