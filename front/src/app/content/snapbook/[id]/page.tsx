import Link from 'next/link';
import { getContentSnapReviews, getFrameDescriptions } from '@/api/snap';
import Icons from '@/components/common/Icons';
import Review from '@/components/snapbook/Review';
import ShareButton from '@/components/snapbook/ShareButton';
import { Frame, Review as ReviewType } from '@/types/snapReview';

interface ReviewData extends ReviewType {
  images: (Frame & { description: string | null })[];
}

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

async function getSnapReviewData(id: string): Promise<{ data: ReviewData | null }> {
  try {
    // TODO: uuid는 실제 로그인된 사용자의 ID로 대체해야 함
    const uuid = 'test-user-id';
    const [reviewResponse, descriptionsResponse] = await Promise.all([
      getContentSnapReviews(id),
      getFrameDescriptions(uuid, id),
    ]);

    const review = reviewResponse.data;
    const descriptions = descriptionsResponse.data;

    // 프레임 설명을 프레임 정보에 매핑
    const imagesWithDescriptions = review.images.map((image) => {
      const description = descriptions.notes.find(
        (note) => note.frameId === image.id,
      );
      return {
        ...image,
        description: description?.description || null,
      };
    });

    return {
      data: {
        ...review,
        images: imagesWithDescriptions,
      },
    };
  } catch (error) {
    console.error('Error fetching snap review:', error);
    return { data: null };
  }
}

const SnapBookDetailPage = async ({ params }: PageProps) => {
  const { id } = await params;
  const { data: snapData } = await getSnapReviewData(id);

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
              {snapData.title}
            </h1>
          </Link>
          <ShareButton />
        </div>
        <Review
          images={snapData.images}
          title={snapData.title}
          date={snapData.date}
        />
      </div>
    </div>
  );
}

export default SnapBookDetailPage;
