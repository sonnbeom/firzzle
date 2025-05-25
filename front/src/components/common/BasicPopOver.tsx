'use client';

import { ReactNode } from 'react';
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover';

interface BasicPopOverProps {
  trigger: ReactNode;
  content: ReactNode;
}

const BasicPopOver = ({ trigger, content }: BasicPopOverProps) => {
  return (
    <Popover>
      <PopoverTrigger asChild>{trigger}</PopoverTrigger>
      <PopoverContent>{content}</PopoverContent>
    </Popover>
  );
};

export default BasicPopOver;
