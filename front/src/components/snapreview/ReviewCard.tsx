import Image from 'next/image';
import React from 'react';

// components
import Icons from '../common/Icons';

const reviews = Array(4)
  .fill(null)
  .map((_, index) => ({
    description:
      '제목을 표기할 때, 얼마나 길어지는게 좋은지, 화면에 보여지는건 어떤지 길이를 확인하기 위해 최대한 늘려보려고...',
    thumbnail: '/assets/images/Firzzle.png',
    flag: index % 2 === 0,
  }));

const ReviewCard = () => {
  return (
    <div className='space-y-4 p-4 md:p-6'>
      <div className='text-sm text-gray-600'>
        사진을 클릭하면 영상 해당 부분이 재생됩니다.
      </div>
      <div className='flex gap-4'>
        {/* 이미지 그룹 */}
        <div className='w-1/3 bg-blue-50 p-4'>
          <div className='space-y-8'>
            {reviews.map((item, idx) => (
              <div
                key={`image-${idx}`}
                className='relative h-[180px] w-full bg-white'
              >
                <div className='absolute inset-0 p-2'>
                  <Image
                    src={item.thumbnail}
                    alt='강의 썸네일'
                    fill
                    sizes='33vw'
                    className='rounded object-cover'
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 텍스트 그룹 */}
        <div className='min-w-0 flex-1'>
          <div className='space-y-8'>
            {reviews.map((item, idx) => (
              <div key={`text-${idx}`} className='relative h-[180px] p-4'>
                <div className='absolute right-0 bottom-[-16px] left-0 border-b border-blue-50'></div>
                {item.flag ? (
                  <textarea
                    className='sm:text-md mt-2 h-[140px] w-full resize-none text-sm text-gray-700 focus:outline-none'
                    defaultValue={item.description}
                  />
                ) : (
                  <p className='sm:text-md mt-2 text-sm break-words text-gray-700'>
                    {item.description}
                  </p>
                )}
                <button
                  className='absolute right-2 bottom-0 p-2'
                  aria-label={item.flag ? 'Upload' : 'Edit'}
                >
                  <Icons
                    id={item.flag ? 'upload' : 'write'}
                    size={24}
                    color={'text-gray-900'}
                  />
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewCard;
