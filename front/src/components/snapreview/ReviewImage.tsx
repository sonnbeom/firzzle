import { Frame } from '@/types/snapReview';
import TimeStamp from '../common/TimeStamp';

interface ReviewImageProps {
  item: Frame;
}

const ReviewImage = ({ item }: ReviewImageProps) => {
  return (
    <div className='relative aspect-video w-full'>
      <TimeStamp time={item.timestamp} type='image' imageUrl={item.imageUrl} />
    </div>
  );
};

export default ReviewImage;
