import { MouseEvent, ReactNode } from 'react';
import { Button } from '../ui/button';
interface BasicButtonProps {
  isDisabled?: boolean;
  title: ReactNode;
  onClick: (e: MouseEvent<HTMLButtonElement>) => void;
  className?: string;
}

const BasicButton = ({
  isDisabled = false,
  title,
  onClick,
  className,
}: BasicButtonProps) => {
  return (
    <Button
      variant={isDisabled ? 'disabled' : 'default'}
      onClick={onClick}
      className={className}
    >
      {title}
    </Button>
  );
};

export default BasicButton;
