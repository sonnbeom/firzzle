import Image from 'next/image';
import React from 'react';

// components
import Icons from '../common/Icons';

const lectures = Array(6).fill({
  title:
    '제목을 표기할 때, 얼마나 길어지는게 좋은지, 화면에 보여지는건 어떤지 길이를 확인하기 위해 최대한 늘려보려고...',
  thumbnail: '/assets/images/Firzzle.png',
});

const Lecture = () => {
  return (
    <div>
      <h2 className='text-center text-lg font-medium text-gray-900 sm:text-xl'>
        <span className='font-semibold text-blue-400'>인공지능</span>에 관련된
        강의를 추천해드릴게요
      </h2>
      <div className='flex justify-end'>
        <button>
          <Icons id='arrow-left' size={24} color={'text-gray-200'} />
        </button>
        <button>
          <Icons id='arrow-right' size={24} color={'text-blue-400'} />
        </button>
      </div>
      <div className='grid grid-cols-2 gap-5 pt-5 sm:grid-cols-3'>
        {lectures.map((item, idx) => (
          <div key={idx}>
            <div className='relative aspect-video w-full max-w-[300px] overflow-hidden rounded-lg border border-gray-200'>
              <Image
                src={item.thumbnail}
                alt='강의 썸네일'
                fill
                sizes='(max-width: 300px) 100vw, 300px'
                className='object-cover'
              />
            </div>
            <p className='mt-2 text-xs text-gray-700'>{item.title}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Lecture;
