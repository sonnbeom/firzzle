'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { checkAndCreateShareCode } from '@/api/share';
import { getContentSnapReview } from '@/api/snap';
import Icons from '@/components/common/Icons';
import { ShareCheck } from '@/types/share';
import { SnapReview } from '@/types/snapReview';
import Review from './Review';
import ShareButton from './ShareButton';

interface SnapBookContentProps {
  contentSeq: string;
}

const SnapBookContent = ({ contentSeq }: SnapBookContentProps) => {
  const router = useRouter();
  const [snapData, setSnapData] = useState<SnapReview | null>(null);
  const [shareData, setShareData] = useState<ShareCheck | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [snapResponse, shareResponse] = await Promise.all([
          getContentSnapReview(contentSeq),
          checkAndCreateShareCode(contentSeq),
        ]);
        setSnapData(snapResponse.data);
        setShareData(shareResponse as ShareCheck);
      } catch (err) {
        setError('데이터를 불러오는데 실패했습니다.');
        console.error('Error fetching data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [contentSeq]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error || !snapData || !shareData) {
    return <div>{error || '데이터를 찾을 수 없습니다.'}</div>;
  }

  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <button
          onClick={() => router.back()}
          className='flex items-center gap-2 text-left'
        >
          <Icons id='arrow-left' className='h-6 w-6 shrink-0' />
          <h1 className='line-clamp-2 text-sm font-semibold break-words text-gray-950 sm:text-lg md:line-clamp-1'>
            {snapData.contentTitle}
          </h1>
        </button>
        <div className='self-center'>
          <ShareButton shareUrl={shareData.shareUrl} />
        </div>
      </div>

      <Review {...snapData} />
    </div>
  );
};

export default SnapBookContent;
