import { Frame } from '@/types/snapReview';

interface ReviewTextFieldProps {
  item: Frame;
  isEditing: boolean;
  handleDescriptionChange: (frameSeq: string, comment: string) => void;
  handleSaveClick: (frameSeq: string, comment: string) => void;
}

const ReviewTextField = ({
  item,
  isEditing,
  handleDescriptionChange,
  handleSaveClick,
}: ReviewTextFieldProps) => {
  return isEditing ? (
    // 수정 모드
    <textarea
      className='lg:text-md h-full w-full flex-1 resize-none border border-blue-50 p-2 text-sm text-gray-700 focus:border-blue-400'
      value={item.comment || ''}
      placeholder='내용을 작성해 주세요.'
      onChange={(e) =>
        handleDescriptionChange(String(item.frameSeq), e.target.value)
      }
      onKeyDown={(e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          handleSaveClick(String(item.frameSeq), item.comment || '');
        }
      }}
    />
  ) : (
    // 읽기 모드
    <div
      className={`h-full overflow-hidden ${!item.comment ? 'flex items-center justify-center' : ''}`}
    >
      <p
        className={`lg:text-md mt-2 line-clamp-5 text-sm whitespace-pre-line text-gray-700 md:text-base lg:line-clamp-none ${!item.comment ? 'text-center' : ''}`}
      >
        {item.comment || '우측 하단의 버튼을 눌러 내용을 작성해주세요.'}
      </p>
    </div>
  );
};

export default ReviewTextField;
