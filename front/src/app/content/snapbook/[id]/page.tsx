import Link from 'next/link';
import Icons from '@/components/common/Icons';
import Review from '@/components/snapbook/Review';
import ShareButton from '@/components/snapbook/ShareButton';

interface ReviewImage {
  src: string;
  description: string;
}

interface SnapItem {
  title: string;
  date: string;
  images: ReviewImage[];
}

// API 연결 이후 변경
const getMockData = async (id: string): Promise<SnapItem> => ({
  title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
  date: '2025.04.23',
  images: [
    {
      src: '/assets/images/Firzzle.png',
      description: '인공지능의 세부 분야와 차이점을 알아봅시다.',
    },
    {
      src: '/assets/images/Firzzle.png',
      description: '비지도 학습의 특징과 클러스터링에 대해 알아봅시다.',
    },
    {
      src: '/assets/images/Firzzle.png',
      description: '강화학습의 기본 개념과 피드백 학습 방식을 소개합니다.',
    },
    {
      src: '/assets/images/Firzzle.png',
      description: '강화학습의 기본 개념과 피드백 학습 방식을 소개합니다.',
    },
  ],
});

async function SnapBookDetailPage({ params }: { params: { id: string } }) {
  const snapData = await getMockData(params.id);

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
