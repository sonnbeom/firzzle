import Link from 'next/link';
import { checkAndCreateShareCode } from '@/api/share';
import { getContentSnapReview } from '@/api/snap';
import Icons from '@/components/common/Icons';
import Review from '@/components/snapbook/Review';
import ShareButton from '@/components/snapbook/ShareButton';
import { ShareCheck } from '@/types/share';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const SnapBookDetailPage = async ({ params }: PageProps) => {
  const { id } = await params;

  const shareData = await checkAndCreateShareCode(Number(id)) as ShareCheck;
  const response = await getContentSnapReview(Number(id));
  const snapData = response.data;

  if (!snapData) {
    return <div>No data found</div>;
  }

  return (
    <div className='container mx-auto px-4'>
      <div className='space-y-6'>
        <div className='flex items-center justify-between'>
          <Link href='/mylearning/snapbook' className='flex items-center gap-2'>
            <Icons id='arrow-left' className='h-8 w-8' />
            <h1 className='text-sm font-semibold text-gray-950 sm:text-lg'>
              {snapData.contentTitle}
            </h1>
          </Link>
          <div className='mt-4'>
            <ShareButton 
              shareUrl={shareData.shareUrl}
            />
          </div>
        </div>
        <Review {...snapData} />
      </div>
    </div>
  );
};

export default SnapBookDetailPage;
