import { getRecommendations } from '@/api/recommend';
import Lecture from '@/components/recommend/Lecture';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Recommend = async ({ params }: PageProps) => {
  const { id } = await params;

  const [lecturesResponse] = await Promise.all([
    getRecommendations(id),
  ]);

  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      <div className='space-y-10 pb-20'>
        <Lecture lectures={lecturesResponse.data} />
      </div>
    </div>
  );
};

export default Recommend;
