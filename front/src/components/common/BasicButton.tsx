import { MouseEvent, ReactNode } from 'react';
import { Button } from '../ui/button';
interface BasicButtonProps {
  isDisabled?: boolean;
  title: ReactNode;
  onClick: (e: MouseEvent<HTMLButtonElement>) => void;
}

const BasicButton = ({
  isDisabled = false,
  title,
  onClick,
}: BasicButtonProps) => {
  return (
    <Button variant={isDisabled ? 'disabled' : 'default'} onClick={onClick}>
      {title}
    </Button>
  );
};

export default BasicButton;
