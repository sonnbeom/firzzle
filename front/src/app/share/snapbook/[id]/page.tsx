import Review from '@/components/snapbook/Review';

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
  ],
});

async function SharedSnapBookPage(props: { params: Promise<{ id: string }> }) {
  const { id } = await props.params;
  const snapData = await getMockData(id);
  
  return (
    <div className='container mx-auto px-4 py-8'>
      <Review
        images={snapData.images}
        title={snapData.title}
        date={snapData.date}
      />
    </div>
  );
}

export default SharedSnapBookPage;
