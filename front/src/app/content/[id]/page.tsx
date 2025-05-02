import { Suspense } from 'react';
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
  const tabParams = await searchParams;
  const tab = tabParams.tab || 'Easy';
  const { data } = await getSummary(id);
  console.log(data);

  return (
    <div className='flex w-full flex-col gap-7'>
      <LevelTab initialTab={tab} />
      <Suspense fallback={<div>Loading...</div>}>
        <SummaryContent
          level={tab}
          easyData={data.easyData}
          highData={data.highData}
        />
      </Suspense>
    </div>
  );
};

export default Summary;
