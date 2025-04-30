import SnapList from '@/components/snapbook/SnapList';

interface SnapItem {
  id: string;
  title: string;
  thumbnail: string;
  date: string;
  length: number;
}

interface DateGroup {
  date: string;
  items: SnapItem[];
}

async function getSnapBookData(): Promise<DateGroup[]> {
  // TODO: 실제 API 호출로 대체
  const mockData: DateGroup[] = [
    {
      date: '2025년 4월 25일',
      items: Array(6).fill({
        id: 'snap1',
        title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
        thumbnail: '/assets/images/Firzzle.png',
        date: '2025년 4월 25일',
        length: 4,
      }),
    },
    {
      date: '2025년 4월 24일',
      items: Array(4).fill({
        id: 'snap2',
        title: '리액트 훅스 완벽 정리',
        thumbnail: '/assets/images/Firzzle.png',
        date: '2025년 4월 24일',
        length: 6,
      }),
    },
    {
      date: '2025년 4월 23일',
      items: Array(4).fill({
        id: 'snap3',
        title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
        thumbnail: '/assets/images/Firzzle.png',
        date: '2025년 4월 23일',
        length: 3,
      }),
    },
    {
      date: '2025년 4월 22일',
      items: Array(4).fill({
        id: 'snap4',
        title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
        thumbnail: '/assets/images/Firzzle.png',
        date: '2025년 4월 23일',
        length: 6,
      }),
    },
  ];

  return mockData;
}

async function SnapBookPage() {
  const snapBookData = await getSnapBookData();

  return <SnapList initialGroups={snapBookData} />;
}

export default SnapBookPage;
