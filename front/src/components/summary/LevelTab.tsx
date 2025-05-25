'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { usePathname } from 'next/navigation';
import { SummaryLevel } from '@/types/summary';
import LevelButton from './LevelButton';

interface LevelTabProps {
  initialTab: SummaryLevel;
}

const LevelTab = ({ initialTab }: LevelTabProps) => {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  // 버튼 활성화 여부
  const isActive = (selectedLevel: SummaryLevel) => {
    return initialTab === selectedLevel;
  };

  // 버튼 클릭 시 탭 변경
  const handleTabChange = (tab: SummaryLevel) => {
    const params = new URLSearchParams(searchParams);
    params.set('tab', tab);

    router.replace(`${pathname}?${params.toString()}`, { scroll: false });
  };

  return (
    <div className='flex w-full gap-2'>
      <LevelButton
        isActive={isActive('Easy')}
        title='쉽게 설명해주세요'
        onClick={() => handleTabChange('Easy')}
      />
      <LevelButton
        isActive={isActive('High')}
        title='배경 지식이 있어요'
        onClick={() => handleTabChange('High')}
      />
    </div>
  );
};

export default LevelTab;
