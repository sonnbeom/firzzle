'use client';

import Image from 'next/image';
import React, { useState } from 'react';

// components
import Icons from '../common/Icons';

// interface
interface Review {
  id: string;
  description: string | null;
  thumbnail: string;
  timestamp: number;
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
      // 나중에 여기에 내용 수정 API 호출 추가
      setLocalReviews((prev) =>
        prev.map((review) =>
          review.id === id ? { ...review, description } : review,
        ),
      );
      setEditingId(null);
    } catch (error) {
      console.error('수정 실패:', error);
    }
  };

  const handleDescriptionChange = (id: string, newDescription: string) => {
    const truncatedDescription = newDescription.slice(0, 250);
    setLocalReviews((prev) =>
      prev.map((review) =>
        review.id === id
          ? { ...review, description: truncatedDescription }
          : review,
      ),
    );
  };

  // 임시 alert, api 연결 이후 youtube api로 영상 재생
  const handleImageClick = (timestamp?: number) => {
    if (timestamp !== undefined) {
      alert(`영상 시간: ${timestamp}초`);
    }
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
                    className='object-cover'
                    onClick={() => handleImageClick(item.timestamp)}
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
                <div className='absolute right-0 bottom-[-16px] left-0 border-b border-blue-50'></div>
                {editingId === item.id ? (
                  <textarea
                    className='md:text-md mt-2 h-[140px] w-full resize-none border border-blue-50 text-sm text-gray-700 focus:border-blue-400'
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
                <div className={`justify-end flex items-center ${editingId === item.id ? '-mt-12 -mr-2' : '-mt-2 -mr-4 '}`}>
                  <span className='text-sm text-gray-500'>
                    ({item.description?.length || 0}/250)
                  </span>
                  <button
                    className='mr-2 p-2'
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
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewCard;
