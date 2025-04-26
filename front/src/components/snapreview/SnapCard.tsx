import Image from 'next/image';
import React from 'react';

const snaps1 = Array(6)
  .fill(null)
  .map((_, index) => ({
    title: 'AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리',
    thumbnail: '/assets/images/Firzzle.png',
    date: Date.now().toString,
  }));

const SnapCard = () => {
  return (
    <div>
      <Image
        src={item.thumbnail}
        alt='강의 썸네일'
        fill
        sizes='33vw'
        className='rounded object-cover'
      />
    </div>
  );
};

export default SnapCard;
