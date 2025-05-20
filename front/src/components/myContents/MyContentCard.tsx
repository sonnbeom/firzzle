import Image from 'next/image';
import Link from 'next/link';
import { formatDate } from '@/utils/formatDate';

interface MyContentCardProps {
  contentSeq: string;
  title: string;
  completedAt: string;
  thumbnailUrl: string;
}

const MyContentCard = ({
  contentSeq,
  title,
  completedAt,
  thumbnailUrl,
}: MyContentCardProps) => {
  return (
    <Link
      href={`/content/${contentSeq}`}
      className='flex w-full gap-3 rounded py-2 hover:bg-gray-50'
    >
      <div className='relative aspect-[16/9] w-[150px]'>
        <Image
          src={thumbnailUrl}
          alt='thumbnail'
          fill
          sizes='100vx'
          className='object-cover'
        />
      </div>
      <div className='flex flex-col justify-between py-3'>
        <p className='line-clamp-2 font-medium text-gray-950'>{title}</p>
        <p className='text-sm text-gray-700'>{formatDate(completedAt)}</p>
      </div>
    </Link>
  );
};

export default MyContentCard;
