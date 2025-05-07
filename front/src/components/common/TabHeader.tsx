'use client';

import { useIsActiveTab } from '@/hooks/useIsActiveTab';
import TabButton from './TabButton';

const TabHeader = () => {
  const { contentSeq, isActive } = useIsActiveTab();

  return (
    <div className='flex w-full justify-around'>
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
        title='추천'
        isActive={isActive('recommend')}
        route={`/content/${contentSeq}/recommend`}
      />
    </div>
  );
};

export default TabHeader;
