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
    <Link href={`/content/${contentSeq}`} className='flex gap-3'>
      <div className='relative aspect-[16/9] w-[150px]'>
        <Image
          src={`${thumbnailUrl}`}
          alt='logo'
          fill
          sizes='100vw,'
          className='object-contain'
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
