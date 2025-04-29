'use client';

import { SummaryLevel } from 'types/summary';
import LevelButton from './LevelButton';

interface LevelTabProps {
  level: SummaryLevel;
  setLevel: (level: SummaryLevel) => void;
}

const LevelTab = ({ level, setLevel }: LevelTabProps) => {
  // 버튼 활성화 여부
  const isActive = (selectedLevel: SummaryLevel) => {
    return level === selectedLevel;
  };

  // 버튼 클릭 시 수준 변경
  const handleLevelClick = (selectedLevel: SummaryLevel) => {
    setLevel(selectedLevel);
  };

  return (
    <div className='flex w-full gap-2'>
      <LevelButton
        isActive={isActive('Easy')}
        title='쉽게 설명해주세요'
        onClick={() => handleLevelClick('Easy')}
      />
      <LevelButton
        isActive={isActive('High')}
        title='배경 지식이 있어요'
        onClick={() => handleLevelClick('High')}
      />
    </div>
  );
};

export default LevelTab;
