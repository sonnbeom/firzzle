import { getContentSnapReview } from '@/api/snap';
import ReviewCard from '@/components/snapreview/ReviewCard';
import { SnapReview } from '@/types/snapReview';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const SnapReviewPage = async ({ params }: PageProps) => {
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
    <div className='relative min-h-screen w-full px-2 md:px-4'>
      <div className='space-y-6'>
        <div className='space-y-10 pb-20'>
          <ReviewCard contentId={id} reviews={snapData.frames || []} />
        </div>
      </div>
    </div>
  );
};

export default SnapReviewPage;
