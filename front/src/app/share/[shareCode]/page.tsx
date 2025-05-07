import { getShareReview } from '@/api/share';
import Review from '@/components/snapbook/Review';

interface PageProps {
  params: Promise<{
    shareCode: string;
  }>;
}

const SharedSnapBookPage = async ({ params }: PageProps) => {
  const { shareCode } = await params;

  try {
    const response = await getShareReview(shareCode);
    if (!response) {
      return <div>No data found</div>;
    }

    return (
      <div className='container mx-auto mt-16 px-4'>
        <div className='h-20'></div>
        <div className='space-y-6'>
          <Review {...response} />
        </div>
      </div>
    );
  } catch (error) {
    console.error('Error fetching shared review:', error);
    return <div>리뷰를 찾을 수 없습니다.</div>;
  }
};

export default SharedSnapBookPage;
