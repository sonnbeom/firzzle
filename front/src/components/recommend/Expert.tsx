import Image from 'next/image';
import React from 'react';

// components
import Icons from '../common/Icons';

const experts = Array(3).fill({
  name: '홍길동',
  description: 'HR Planning & Analytics, Design Thinking 전문가',
  thumbnail: '/assets/images/AI Playground.png',
});

const Expert = () => {
  return (
    <div>
      <h2 className='text-center text-lg font-medium text-gray-900 sm:text-xl'>
        <span className='font-semibold text-blue-400'>인공지능</span> 전문가와
        대화해보세요
      </h2>
      <div className='flex justify-end'>
        <button>
          <Icons id='arrow-left' size={24} color={'text-blue-400'} />
        </button>
        <button>
          <Icons id='arrow-right' size={24} color={'text-gray-200'} />
        </button>
      </div>
      <div className='mt-4 grid grid-cols-2 gap-4 sm:grid-cols-3'>
        {experts.map((item, idx) => (
          <div
            key={idx}
            className='flex flex-col items-center rounded-xl p-4 shadow'
            style={{
              background:
                'linear-gradient(to bottom, #324eef 50%, #ffffff 50%)',
            }}
          >
            <div className='relative h-[100px] w-[100px] overflow-hidden rounded-full border border-gray-200'>
              <Image
                src={item.thumbnail}
                alt='전문가 이미지'
                fill
                sizes='100px'
                className='object-cover'
              />
            </div>
            <p className='mt-2 font-semibold text-gray-700'>{item.name}</p>
            <p className='mt-2 mb-2 text-center text-xs text-gray-700'>
              {item.description}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Expert;
