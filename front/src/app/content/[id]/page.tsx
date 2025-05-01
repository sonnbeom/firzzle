import SummaryContainer from '@/components/summary/SummaryContainer';
import { getSummary } from '@/api/summary';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Summary = async ({ params }: PageProps) => {
  const { id } = await params;
  const { data } = await getSummary(id);

  return <SummaryContainer easyData={data.easyData} highData={data.highData} />;
};

export default Summary;
