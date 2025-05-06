import SearchBar from '@/components/common/SearchBar';
import MyContentCard from '@/components/myContents/MyContentCard';
import { ContentResponse } from '@/types/content';

const MyContentsPage = () => {
  const dummayData: ContentResponse[] = [
    {
      contentSeq: '9',
      title: 'AI 윤리와 편향성 이해하기',
      description:
        'AI 시스템의 윤리적 문제와 알고리즘 편향성에 대한 기본 개념을 설명합니다.',
      contentType: '교육/사회',
      videoId: 'vidD567890',
      url: 'https://youtube.com/watch?v=vidD567890',
      thumbnailUrl: 'https://img.youtube.com/vi/vidD567890/mqdefault.jpg',
      duration: 2500,
      tags: 'AI윤리,알고리즘편향,책임있는AI',
      processStatus: 'C',
      analysisData: '',
      transcript: '',
      indate: '2025-05-10T13:00:00',
      completedAt: '2025-05-10T14:00:00',
      deleteYn: 'N',
      formattedDuration: '00:41:40',
      processStatusText: '완료',
    },
    {
      contentSeq: '8',
      title: '컴퓨터 비전 기초와 응용',
      description: '이미지 인식 기술의 원리와 실생활 적용 사례를 소개합니다.',
      contentType: '교육/IT',
      videoId: 'vidC345678',
      url: 'https://youtube.com/watch?v=vidC345678',
      thumbnailUrl: 'https://img.youtube.com/vi/vidC345678/mqdefault.jpg',
      duration: 3500,
      tags: '컴퓨터비전,CNN,이미지인식',
      processStatus: 'C',
      analysisData: '',
      transcript: '',
      indate: '2025-05-07T14:00:00',
      completedAt: '2025-05-07T15:00:00',
      deleteYn: 'N',
      formattedDuration: '00:58:20',
      processStatusText: '완료',
    },
    {
      contentSeq: '7',
      title: '컴퓨터 비전 기초와 응용',
      description: '이미지 인식 기술의 원리와 실생활 적용 사례를 소개합니다.',
      contentType: '교육/IT',
      videoId: 'vidC345678',
      url: 'https://youtube.com/watch?v=vidC345678',
      thumbnailUrl: 'https://img.youtube.com/vi/vidC345678/mqdefault.jpg',
      duration: 3500,
      tags: '컴퓨터비전,CNN,이미지인식',
      processStatus: 'C',
      analysisData: '',
      transcript: '',
      indate: '2025-05-07T14:00:00',
      completedAt: '2025-05-07T15:00:00',
      deleteYn: 'N',
      formattedDuration: '00:58:20',
      processStatusText: '완료',
    },
  ];

  return (
    <div className='flex flex-col items-center gap-12'>
      <SearchBar />
      <div className='grid w-full grid-cols-2 gap-8'>
        {dummayData &&
          dummayData.length > 0 &&
          dummayData.map((item) => (
            <MyContentCard
              key={item.contentSeq}
              contentSeq={item.contentSeq}
              title={item.title}
              completedAt={item.completedAt}
              thumbnailUrl={item.thumbnailUrl}
            />
          ))}
      </div>
    </div>
  );
};

export default MyContentsPage;
