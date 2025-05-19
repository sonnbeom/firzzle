'use client';

import { useIsActiveTab } from '@/hooks/useIsActiveTab';
import TabButton from './TabButton';

const SideMenu = () => {
  const { isActive } = useIsActiveTab();

  return (
    <div className='flex w-full items-start gap-4 xl:flex-col'>
      <TabButton
        title='사진첩'
        isActive={isActive('/mylearning/snapbook')}
        route='/mylearning/snapbook'
        iconId='snapbook'
        className='items-start'
      />

      <TabButton
        title='최근 학습 내역'
        isActive={isActive('/mylearning/contents')}
        route='/mylearning/contents'
        iconId='content'
        className='items-start'
      />
    </div>
  );
};

export default SideMenu;
