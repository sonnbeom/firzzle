'use client';

import { useState } from 'react';
import { updateFrameComments } from '@/api/snap';
import {
  UpdateFrameCommentsRequest,
  UpdateFrameCommentsResponse,
  Frame,
} from '@/types/snapReview';
import { MAX_SNAP_REVIEW_LENGTH } from 'utils/const';
import Icons from '../common/Icons';
import TimeStamp from '../common/TimeStamp';

interface ReviewCardProps {
  contentId: string;
  reviews: Frame[];
}

const ReviewCard = ({ contentId, reviews }: ReviewCardProps) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [localReviews, setLocalReviews] = useState<
    Frame[] | UpdateFrameCommentsResponse[]
  >(reviews);

  const handleEditClick = (id: string) => {
    setEditingId(id);
  };

  const handleSaveClick = async (id: string, description: string) => {
    try {
      const request: UpdateFrameCommentsRequest = {
        frames: localReviews.map((review) => ({
          frameSeq: review.frameSeq,
          comment:
            review.frameSeq === Number(id)
              ? description
              : ('comment' in review ? review.comment : null) || '',
        })),
      };

      const response = await updateFrameComments(Number(contentId), request);

      // 응답으로 받은 데이터로 상태 업데이트
      setLocalReviews(response.data);
      setEditingId(null);
    } catch (error) {
      console.error('수정 실패:', error);
    }
  };

  const handleDescriptionChange = (id: string, newDescription: string) => {
    const truncatedDescription = newDescription.slice(
      0,
      MAX_SNAP_REVIEW_LENGTH,
    );
    setLocalReviews((prev) =>
      prev.map((review) =>
        review.frameSeq === Number(id)
          ? { ...review, comment: truncatedDescription }
          : review,
      ),
    );
  };

  return (
    <div className='space-y-4 p-4 lg:p-6'>
      <div className='text-xs text-gray-600 md:text-sm'>
        사진을 클릭하면 영상 해당 부분이 재생됩니다.
      </div>
      <div className='flex gap-4'>
        {/* 이미지 그룹 */}
        <div className='w-1/2 bg-blue-50 p-4 md:w-2/5'>
          <div className='space-y-8'>
            {localReviews.map((item) => (
              <div
                key={`image-${item.frameSeq}`}
                className='relative h-[180px] w-full'
              >
                <TimeStamp
                  time={item.timestamp}
                  type='image'
                  imageUrl={item.imageUrl}
                  width={600}
                  height={400}
                />
              </div>
            ))}
          </div>
        </div>

        {/* 텍스트 그룹 */}
        <div className='min-w-0 flex-1'>
          <div className='space-y-8'>
            {localReviews.map((item) => (
              <div
                key={`text-${item.frameSeq}`}
                className='relative h-[180px] p-4'
              >
                <div className='absolute right-0 bottom-[-16px] left-0 border-b border-blue-50'></div>
                {editingId === String(item.frameSeq) ? (
                  <textarea
                    className='lg:text-md mt-2 h-[140px] w-full resize-none border border-blue-50 text-sm text-gray-700 focus:border-blue-400'
                    value={item.comment || ''}
                    placeholder='내용을 작성해 주세요.'
                    onChange={(e) =>
                      handleDescriptionChange(
                        String(item.frameSeq),
                        e.target.value,
                      )
                    }
                  />
                ) : (
                  <div
                    className={`h-full overflow-hidden ${!item.comment ? 'flex items-center justify-center' : ''}`}
                  >
                    <p
                      className={`lg:text-md mt-2 line-clamp-5 text-sm text-gray-700 md:line-clamp-6 md:text-sm lg:line-clamp-none ${!item.comment ? 'text-center' : ''}`}
                    >
                      {item.comment || '내용을 작성해 주세요.'}
                    </p>
                  </div>
                )}
                <div className='-mt-2 -mr-4 flex items-center justify-end'>
                  <span className='lg:text-md text-xs text-gray-500 md:text-sm'>
                    ({item.comment?.length || 0}/{MAX_SNAP_REVIEW_LENGTH})
                  </span>
                  <button
                    className='mr-2 p-2'
                    onClick={() =>
                      editingId === String(item.frameSeq)
                        ? handleSaveClick(
                            String(item.frameSeq),
                            item.comment || '',
                          )
                        : handleEditClick(String(item.frameSeq))
                    }
                    aria-label={
                      editingId === String(item.frameSeq) ? 'Save' : 'Edit'
                    }
                  >
                    <div className='hidden md:block lg:hidden'>
                      <Icons
                        id={
                          editingId === String(item.frameSeq)
                            ? 'upload'
                            : 'write'
                        }
                        size={20}
                        color={'text-gray-900'}
                      />
                    </div>
                    <div className='hidden lg:block'>
                      <Icons
                        id={
                          editingId === String(item.frameSeq)
                            ? 'upload'
                            : 'write'
                        }
                        size={24}
                        color={'text-gray-900'}
                      />
                    </div>
                    <div className='block md:hidden'>
                      <Icons
                        id={
                          editingId === String(item.frameSeq)
                            ? 'upload'
                            : 'write'
                        }
                        size={18}
                        color={'text-gray-900'}
                      />
                    </div>
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
