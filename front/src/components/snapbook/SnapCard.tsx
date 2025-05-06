import Image from 'next/image';
import { SnapReview } from '@/types/snapReview';

interface SnapCardProps {
  data: SnapReview;
}

const SnapCard = ({ data }: SnapCardProps) => {
  return (
    <div className='group mx-auto max-w-[240px] overflow-hidden rounded-lg bg-white shadow-md transition-all'>
      <div className='relative aspect-video w-full bg-white'>
        <Image
          src={data.thumbnailUrl}
          alt={data.contentTitle}
          fill
          sizes='240px'
          className='object-contain'
        />
      </div>

      <div className='p-4'>
          <span className='text-md font-semibold text-gray-950 md:text-xl'>
            스냅 {data.frameCount || 0}컷
          </span>
        <h3 className='mb-2 line-clamp-2 text-lg'>{data.contentTitle}</h3>
      </div>
    </div>
  );
};

export default SnapCard;
