import ReviewCard from '@/components/snapreview/ReviewCard';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const SnapReviewPage = async ({ params }: PageProps) => {
  const { id } = await params;

  return (
    <div className='relative min-h-screen w-full px-2 md:px-4'>
      <div className='space-y-6'>
        <div className='space-y-10 pb-20'>
          <ReviewCard contentId={id} />
        </div>
      </div>
    </div>
  );
};

export default SnapReviewPage;
