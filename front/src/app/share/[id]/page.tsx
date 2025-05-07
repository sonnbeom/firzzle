import { getContentSnapReview } from '@/api/snap';
import Review from '@/components/snapbook/Review';
import { SnapReview } from '@/types/snapReview';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const SharedSnapBookPage = async ({ params }: PageProps) => {
  const { id } = await params;

  async function getSnapReviewData(id: string): Promise<SnapReview | null> {
    try {
      const response = await getContentSnapReview(Number(id));
      return response.data;
    } catch (error) {
      console.error('Error fetching snap review:', error);
      return null;
    }
  }

  const snapData = await getSnapReviewData(id);

  if (!snapData) {
    return <div>No data found</div>;
  }

  return (
    <div className='container mx-auto mt-10 px-4'>
      <div className='space-y-6'>
        <Review {...snapData} />
      </div>
    </div>
  );
};

export default SharedSnapBookPage;
