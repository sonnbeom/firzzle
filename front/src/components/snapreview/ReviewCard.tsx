'use client';

import { useEffect, useState } from 'react';
import { getContentSnapReview, updateFrameComments } from '@/api/snap';
import { UpdateFrameCommentsRequest, Frame } from '@/types/snapReview';
import { MAX_SNAP_REVIEW_LENGTH } from 'utils/const';
import TimeStamp from '../common/TimeStamp';
import ReviewLoading from './ReviewLoading';
import ReviewTextField from './ReviewTextField';
import ReviewWriteButton from './ReviewWriteButton';
interface ReviewCardProps {
  contentId: string;
}

const ReviewCard = ({ contentId }: ReviewCardProps) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [localReviews, setLocalReviews] = useState<Frame[] | null>(null);

  useEffect(() => {
    const fetchReviews = async () => {
      try {
        const response = await getContentSnapReview(contentId);
        setLocalReviews(response.data.frames);
      } catch (error) {
        console.error('Error fetching reviews:', error);
      }
    };
    fetchReviews();
  }, [contentId]);

  const handleEditClick = (id: string) => {
    setEditingId(id);
  };

  // 작성한 내용 저장
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

      const response = await updateFrameComments(contentId, request);

      // 응답으로 받은 데이터로 상태 업데이트
      setLocalReviews(response.data);
      setEditingId(null);
    } catch (error) {
      console.error('수정 실패:', error);
    }
  };

  // 읽기 모드로 전환
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
    <div className='flex flex-col gap-4 p-4 lg:p-6'>
      {localReviews === null || localReviews.length === 0 ? (
        <ReviewLoading />
      ) : (
        <div className='text-xs text-gray-600 md:text-sm'>
          사진을 클릭하면 영상 해당 부분이 재생됩니다.
        </div>
      )}
      {localReviews && localReviews.length > 0 && (
        <div className='flex flex-col justify-around'>
          {localReviews.map((item) => (
            <div key={`image-${item.frameSeq}`} className='flex gap-4'>
              {/* 이미지 그룹 */}
              <div className='flex w-1/2 bg-blue-50 p-4 md:w-2/5 md:justify-start md:gap-4'>
                <div className='relative aspect-video w-full'>
                  <TimeStamp
                    time={item.timestamp}
                    type='image'
                    imageUrl={item.imageUrl}
                  />
                </div>
              </div>

              <div className='flex min-w-0 flex-1 flex-col justify-around border-b border-blue-50 py-4'>
                <ReviewTextField
                  isEditing={editingId === String(item.frameSeq)}
                  item={item}
                  handleDescriptionChange={handleDescriptionChange}
                  handleSaveClick={handleSaveClick}
                />
                <ReviewWriteButton
                  isEditing={editingId === String(item.frameSeq)}
                  item={item}
                  handleSaveClick={handleSaveClick}
                  handleEditClick={handleEditClick}
                />
              </div>
            </div>
          ))}
          {/* 텍스트 그룹 */}
        </div>
      )}
    </div>
  );
};

export default ReviewCard;
