'use client';

import SideMenuCard from './SideMenuCard';
import TabButton from './TabButton';

const SideMenu = () => {
  const dummyData = [
    {
      id: '1',
      thumbnail: 'https://picsum.photos/360/640',
      title: '사진첩',
      date: '2024-01-01',
    },
    {
      id: '2',
      thumbnail: 'https://picsum.photos/360/640',
      title: '사진첩',
      date: '2024-01-01',
    },
  ];

  return (
    <div className='flex w-full flex-col items-start gap-3'>
      <TabButton
        title='사진첩'
        isActive={true}
        onClick={() => {}}
        iconId='snapbook'
      />

      <hr className='w-full border border-gray-100' />

      <TabButton
        title='최근 학습 내역'
        isActive={false}
        onClick={() => {}}
        iconId='content'
      />

      <div className='flex flex-col gap-4 px-3'>
        {dummyData &&
          dummyData.length > 0 &&
          dummyData.map((item) => <SideMenuCard key={item.id} {...item} />)}
      </div>
    </div>
  );
};

export default SideMenu;
