import { getRecommendations, getExpertRecommendations } from '@/api/recommend';
import Expert from '@/components/recommend/Expert';
import Lecture from '@/components/recommend/Lecture';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Recommend = async ({ params }: PageProps) => {
  const { id } = await params;

  const [lecturesResponse, expertsResponse] = await Promise.all([
    getRecommendations(id),
    getExpertRecommendations(id),
  ]);

  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      <div className='space-y-10 pb-20'>
        <Lecture lectures={lecturesResponse.data} />
        <Expert experts={expertsResponse.data} />
      </div>
    </div>
  );
};

export default Recommend;
