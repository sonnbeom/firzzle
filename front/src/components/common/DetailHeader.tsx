'use client';

import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { postTransitionLog } from '@/api/log';
import { useIsActiveTab } from '@/hooks/useIsActiveTab';
import { determinePathType } from '@/utils/determinePathType';
import LearningChatDrawer from '../learningChat/LearningChatDrawer';
import Icons from './Icons';
import TabButton from './TabButton';

const DetailHeader = () => {
  const { contentSeq, isActive } = useIsActiveTab();
  const pathname = usePathname();
  const [pathType, setPathType] = useState('');
  const [isLearningChatOpen, setIsLearningChatOpen] = useState(false);

  useEffect(() => {
    setPathType(determinePathType(pathname));

    const transitionLog = async () => {
      await postTransitionLog({
        fromContent: pathType, // 이전 페이지
        toContent: determinePathType(pathname), // 현재 페이지
      });
    };

    transitionLog();
  }, [pathname]);

  return (
    <>
      <div className='flex w-full justify-around gap-4 overflow-x-auto pb-1 lg:overflow-x-hidden lg:pb-0'>
        <button
          onClick={() => setIsLearningChatOpen(true)}
          className='block lg:hidden'
        >
          <Icons id='learningChat' color='text-gray-950' size={28} />
        </button>
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
      <LearningChatDrawer
        isOpen={isLearningChatOpen}
        onClose={() => setIsLearningChatOpen(false)}
        contentId={contentSeq}
      />
    </>
  );
};

export default DetailHeader;
