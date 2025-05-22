import { getShareReview } from '@/api/share';
import BasicToaster from '@/components/common/BasicToaster';
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
      <div className='container mx-auto mt-10 px-4'>
        <div className='h-4'></div>
        <div className='space-y-6'>
          <Review {...response} />
        </div>
      </div>
    );
  } catch (error) {
    BasicToaster.error(error.message, {
      id: 'share',
      duration: 2000,
    });
  }
};

export default SharedSnapBookPage;
