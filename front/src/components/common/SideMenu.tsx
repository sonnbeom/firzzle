'use client';

import { useIsActiveTab } from '@/hooks/useIsActiveTab';
import TabButton from './TabButton';

const SideMenu = () => {
  const { isActive } = useIsActiveTab();

  return (
    <div className='flex w-full flex-col items-start gap-1'>
      <TabButton
        title='사진첩'
        isActive={isActive('snapbook')}
        route='/snapbook'
        iconId='snapbook'
      />

      <TabButton
        title='최근 학습 내역'
        isActive={isActive('contents')}
        route='/contents'
        iconId='content'
      />
    </div>
  );
};

export default SideMenu;
