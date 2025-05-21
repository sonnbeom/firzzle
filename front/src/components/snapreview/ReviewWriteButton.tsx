import { Frame } from '@/types/snapReview';
import { MAX_SNAP_REVIEW_LENGTH } from '@/utils/const';
import Icons from '../common/Icons';

interface ReviewWriteButtonProps {
  item: Frame;
  isEditing: boolean;
  handleSaveClick: (frameSeq: string, comment: string) => void;
  handleEditClick: (frameSeq: string) => void;
}

const ReviewWriteButton = ({
  item,
  isEditing,
  handleSaveClick,
  handleEditClick,
}: ReviewWriteButtonProps) => {
  return (
    <div className='flex items-center justify-end'>
      <span className='text-sm text-gray-500 md:text-base'>
        ({item.comment?.length || 0}/{MAX_SNAP_REVIEW_LENGTH})
      </span>
      <button
        className='cursor-pointer p-2'
        onClick={() =>
          isEditing
            ? handleSaveClick(String(item.frameSeq), item.comment || '')
            : handleEditClick(String(item.frameSeq))
        }
        aria-label={isEditing ? 'Save' : 'Edit'}
      >
        {/* xl 이상 */}
        <div className='hidden lg:block'>
          <Icons
            id={isEditing ? 'upload' : 'write'}
            size={24}
            color={'text-gray-900'}
          />
        </div>
        {/* md 이상 */}
        <div className='hidden md:block lg:hidden'>
          <Icons
            id={isEditing ? 'upload' : 'write'}
            size={20}
            color={'text-gray-900'}
          />
        </div>
        {/* 기본 */}
        <div className='block md:hidden'>
          <Icons
            id={isEditing ? 'upload' : 'write'}
            size={18}
            color={'text-gray-900'}
          />
        </div>
      </button>
    </div>
  );
};

export default ReviewWriteButton;
