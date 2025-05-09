'use client';

import ContentCard from '@/components/admin/ContentCard';
import DateRangeSelector from '@/components/admin/DateRangeSelector';
import FavoriteContentCard from '@/components/admin/FavoriteContentCard';
import PopularCategoryCard from '@/components/admin/PopularCategoryCard';
import PopularExpertCard from '@/components/admin/PopularExpertCard';
import VideoLengthCard from '@/components/admin/VideoLengthCard';

const AdminPage = () => {
  const handleDateChange = ({
    startDate,
    endDate,
  }: {
    startDate: Date;
    endDate: Date;
  }) => {
    // 추후 여기서 데이터 fetching 등을 수행
  };

  return (
    <div className='flex flex-col gap-6 p-6'>
      <DateRangeSelector onChange={handleDateChange} />
      <ContentCard
        title='인기 있는 영상 카테고리'
        description='2025.01 ~ 2025.04'
      >
        <PopularCategoryCard />
      </ContentCard>

      <div className='flex w-full gap-6'>
        <div className='w-1/2'>
          <ContentCard title='영상 길이 분포'>
            <VideoLengthCard />
          </ContentCard>
        </div>
        <div className='flex-1'>
          <ContentCard title='선호 컨텐츠'>
            <FavoriteContentCard />
          </ContentCard>
        </div>
      </div>

      <ContentCard title='인기 있는 강의자'>
        <div className='flex gap-4'>
          <PopularExpertCard
            name='백정순'
            description='AI 연구소장\nHR Planning & Analytics, Design\n3년간 강의'
            tags={['AI', 'HR', 'Design']}
          />
          <PopularExpertCard
            name='백정순'
            description='AI 연구소장\nHR Planning & Analytics, Design\n3년간 강의'
            tags={['AI', 'HR', 'Design']}
          />
          <PopularExpertCard
            name='백정순'
            description='AI 연구소장\nHR Planning & Analytics, Design\n3년간 강의'
            tags={['AI', 'HR', 'Design']}
          />
          <PopularExpertCard
            name='백정순'
            description='AI 연구소장\nHR Planning & Analytics, Design\n3년간 강의'
            tags={['AI', 'HR', 'Design']}
          />
        </div>
      </ContentCard>
    </div>
  );
};

export default AdminPage;
