import Link from 'next/link';
import { getContentSnapReview } from '@/api/snap';
import Icons from '@/components/common/Icons';
import Review from '@/components/snapbook/Review';
import ShareButton from '@/components/snapbook/ShareButton';
import { SnapReview } from '@/types/snapReview';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const SnapBookDetailPage = async ({ params }: PageProps) => {
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
    <div className='container mx-auto px-4'>
      <div className='space-y-6'>
        <div className='flex items-center justify-between'>
          <Link href='/content/snapbook' className='flex items-center gap-2'>
            <Icons id='arrow-left' className='h-8 w-8' />
            <h1 className='text-sm font-semibold text-gray-950 sm:text-lg'>
              {snapData.contentTitle}
            </h1>
          </Link>
          <ShareButton />
        </div>
        <Review {...snapData} />
      </div>
    </div>
  );
};

export default SnapBookDetailPage;
