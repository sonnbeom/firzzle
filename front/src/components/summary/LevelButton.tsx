'use client';

import { Button } from '../ui/button';

interface LevelButtonProps {
  isActive?: boolean;
  title: string;
  onClick: () => void;
}

const LevelButton = ({
  isActive = false,
  title,
  onClick,
}: LevelButtonProps) => {
  return (
    <Button
      variant={isActive ? 'default' : 'ghost'}
      onClick={onClick}
      className={`${isActive ? 'cursor-default' : 'cursor-pointer'} flex-1`}
    >
      {title}
    </Button>
  );
};

export default LevelButton;
