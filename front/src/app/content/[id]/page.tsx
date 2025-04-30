import SummaryContainer from '@/components/summary/SummaryContainer';
import { getSummary } from '@/api/summary';

interface PageProps {
  params: {
    id: string;
  };
}

const Summary = async ({ params }: PageProps) => {
  const awaitedParams = await params;
  const { data } = await getSummary(awaitedParams.id);

  return <SummaryContainer easyData={data.easyData} highData={data.highData} />;
};

export default Summary;
