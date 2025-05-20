import Image from 'next/image';
import Link from 'next/link';
import { formatDate } from '@/utils/formatDate';

interface MyContentCardProps {
  contentSeq: string;
  title: string;
  completedAt: string;
  thumbnailUrl: string;
  processStatus: 'Q' | 'P' | 'C' | 'F';
}

const MyContentCard = ({
  contentSeq,
  title,
  completedAt,
  thumbnailUrl,
  processStatus,
}: MyContentCardProps) => {
  const content = (
    <div
      className={`flex w-full gap-3 rounded py-2 ${
        processStatus !== 'C' ? 'bg-gray-100 px-1' : ''
      }`}
    >
      <div className='relative h-[90px] w-[160px]'>
        <Image
          src={thumbnailUrl}
          alt='thumbnail'
          width={160}
          height={90}
          className='h-full w-full object-cover'
        />
      </div>
      <div className='flex flex-col justify-between py-3'>
        <p className='line-clamp-2 font-medium text-gray-950'>{title}</p>
        <p className='text-sm text-gray-700'>
          {processStatus === 'C' ? formatDate(completedAt) : '컨텐츠 생성 중'}
        </p>
      </div>
    </div>
  );

  if (processStatus === 'C') {
    return (
      <Link href={`/content/${contentSeq}`} className='hover:bg-gray-50'>
        {content}
      </Link>
    );
  }

  return content;
};

export default MyContentCard;
