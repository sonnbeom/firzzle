'use client';

import { usePathname, useRouter } from 'next/navigation';
import TabButton from './TabButton';

const TabHeader = () => {
  const pathname = usePathname();
  const router = useRouter();
  const id = 1;

  const isActive = (path: string) => {
    const lastPath = pathname.split('/').pop();
    return lastPath === path;
  };

  return (
    <div className='flex w-full justify-between gap-4'>
      <TabButton
        title='요약 노트'
        isActive={isActive(`${id}`)}
        onClick={() => router.push(`/content/${id}`)}
      />
      <TabButton
        title='AI 퀴즈'
        isActive={isActive('quiz')}
        onClick={() => router.push(`/content/${id}/quiz`)}
      />
      <TabButton
        title='스냅 리뷰'
        isActive={isActive('snapreview')}
        onClick={() => router.push(`/content/${id}/snapreview`)}
      />
      <TabButton
        title='추천'
        isActive={isActive('recommend')}
        onClick={() => router.push(`/content/${id}/recommend`)}
      />
    </div>
  );
};

export default TabHeader;
