import { getContentSnapReviews, getFrameDescriptions } from '@/api/snap';
import ReviewCard from '@/components/snapreview/ReviewCard';
import { Frame, Review as ReviewType } from '@/types/snapReview';

// 기본 Review 타입에 description이 추가된 images 배열을 포함
interface ReviewData extends ReviewType {
  images: (Frame & { description: string | null })[];
}

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

async function getSnapReviewData(
  id: string,
): Promise<{ data: ReviewData | null }> {
  try {
    // TODO: uuid는 실제 로그인된 사용자의 ID로 대체해야 함
    const uuid = 'test-user-id';

    // 리뷰 정보와 프레임 설명을 병렬로 가져옴
    const [reviewResponse, descriptionsResponse] = await Promise.all([
      getContentSnapReviews(id),
      getFrameDescriptions(uuid, id),
    ]);

    const review = reviewResponse.data;
    const descriptions = descriptionsResponse.data;

    // 각 이미지에 해당하는 설명을 찾아서 합침
    const imagesWithDescriptions = review.images.map((image) => ({
      ...image,
      description:
        descriptions.notes.find((note) => note.frameId === image.id)
          ?.description || null,
    }));

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

const SnapReviewPage = async ({ params }: PageProps) => {
  const { id } = await params;
  const { data: snapData } = await getSnapReviewData(id);

  if (!snapData) {
    return <div>No data found</div>;
  }

  return (
    <div className='relative min-h-screen w-full px-2 md:px-4'>
      <div className='space-y-6'>
        <div className='space-y-10 pb-20'>
          <ReviewCard
            contentId={id}
            reviews={snapData.images.map((image) => ({
              id: image.id,
              description: image.description,
              thumbnail: image.src,
              timestamp: image.timestamp,
            }))}
          />
        </div>
      </div>
    </div>
  );
};

export default SnapReviewPage;
