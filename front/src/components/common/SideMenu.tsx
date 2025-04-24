'use client';

import TabButton from './TabButton';

const SideMenu = () => {
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

      <div></div>
    </div>
  );
};

export default SideMenu;
