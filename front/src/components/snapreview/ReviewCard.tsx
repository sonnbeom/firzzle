'use client';

import Image from 'next/image';
import React, { useState } from 'react';

// components
import Icons from '../common/Icons';

interface Review {
  id: string;
  description: string | null;
  thumbnail: string;
}

interface ReviewCardProps {
  reviews: Review[];
}

const ReviewCard = ({ reviews }: ReviewCardProps) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [localReviews, setLocalReviews] = useState(reviews);

  const handleEditClick = (id: string) => {
    setEditingId(id);
  };

  const handleSaveClick = async (id: string, description: string) => {
    try {
      // 나중에 여기에 API 호출 추가
      setLocalReviews((prev) =>
        prev.map((review) =>
          review.id === id ? { ...review, description } : review,
        ),
      );
      setEditingId(null);
    } catch (error) {
      console.error('Failed to update review:', error);
    }
  };

  const handleDescriptionChange = (id: string, newDescription: string) => {
    setLocalReviews((prev) =>
      prev.map((review) =>
        review.id === id ? { ...review, description: newDescription } : review,
      ),
    );
  };
  return (
    <div className='space-y-4 p-4 md:p-6'>
      <div className='text-sm text-gray-600'>
        사진을 클릭하면 영상 해당 부분이 재생됩니다.
      </div>
      <div className='flex gap-4'>
        {/* 이미지 그룹 */}
        <div className='w-1/3 bg-blue-50 p-4'>
          <div className='space-y-8'>
            {localReviews.map((item) => (
              <div
                key={`image-${item.id}`}
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
            {localReviews.map((item) => (
              <div key={`text-${item.id}`} className='relative h-[180px] p-4'>
                {editingId === item.id && (
                  <div className='absolute right-0 bottom-[-16px] left-0 border-b border-blue-50'></div>
                )}
                {editingId === item.id ? (
                  <textarea
                    className='md:text-md mt-2 h-[140px] w-full resize-none border border-gray-300 text-sm text-gray-700 focus:border-blue-400'
                    value={item.description || ''}
                    placeholder='내용을 작성해 주세요.'
                    onChange={(e) =>
                      handleDescriptionChange(item.id, e.target.value)
                    }
                  />
                ) : (
                  <div
                    className={`h-full ${!item.description ? 'flex items-center justify-center' : ''}`}
                  >
                    <p
                      className={`md:text-md mt-2 text-sm break-words text-gray-700 ${!item.description ? 'text-center' : ''}`}
                    >
                      {item.description || '내용을 작성해 주세요.'}
                    </p>
                  </div>
                )}
                <button
                  className='absolute right-2 bottom-2 p-4'
                  onClick={() =>
                    editingId === item.id
                      ? handleSaveClick(item.id, item.description || '')
                      : handleEditClick(item.id)
                  }
                  aria-label={editingId === item.id ? 'Save' : 'Edit'}
                >
                  <Icons
                    id={editingId === item.id ? 'upload' : 'write'}
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
