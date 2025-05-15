'use client';

import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useIsActiveTab } from '@/hooks/useIsActiveTab';
import { determinePathType } from '@/utils/determinePathType';
import TabButton from './TabButton';

const DetailHeader = () => {
  const { contentSeq, isActive } = useIsActiveTab();
  const pathname = usePathname();
  const [pathType, setPathType] = useState('');

  useEffect(() => {
    setPathType(determinePathType(pathname));
    console.log('pathType', pathType);
  }, [pathname]);

  return (
    <div className='flex w-full justify-around gap-4 overflow-x-auto pb-1 lg:overflow-x-hidden lg:pb-0'>
      <TabButton
        title='러닝 채팅'
        isActive={isActive('learningchat')}
        route={`/content/${contentSeq}/learningchat`}
        className='block lg:hidden'
      />
      <TabButton
        title='요약 노트'
        isActive={isActive(contentSeq)}
        route={`/content/${contentSeq}`}
      />
      <TabButton
        title='AI 퀴즈'
        isActive={isActive('quiz')}
        route={`/content/${contentSeq}/quiz`}
      />
      <TabButton
        title='스냅 리뷰'
        isActive={isActive('snapreview')}
        route={`/content/${contentSeq}/snapreview`}
      />
      <TabButton
        title='관련 컨텐츠'
        isActive={isActive('recommend')}
        route={`/content/${contentSeq}/recommend`}
      />
    </div>
  );
};

export default DetailHeader;
