'use client';

import { useIsActiveTab } from '@/hooks/useIsActiveTab';
import TabButton from './TabButton';

const SideMenu = () => {
  const { isActive } = useIsActiveTab();

  return (
    <div className='flex w-full items-start gap-4 xl:flex-col'>
      <TabButton
        title='사진첩'
        isActive={isActive('snapbook')}
        route='/mylearning/snapbook'
        iconId='snapbook'
      />

      <TabButton
        title='최근 학습 내역'
        isActive={isActive('contents')}
        route='/mylearning/contents'
        iconId='content'
      />
    </div>
  );
};

export default SideMenu;
