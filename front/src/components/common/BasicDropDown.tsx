'use client';

import * as React from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

interface DropDownItem {
  label: string;
  onClick?: () => void;
  disabled?: boolean;
}

interface BasicDropDownProps {
  trigger: React.ReactNode;
  items: DropDownItem[];
}

const BasicDropDown = ({ trigger, items }: BasicDropDownProps) => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>{trigger}</DropdownMenuTrigger>
      <DropdownMenuContent
        align='center'
        side='bottom'
        className='min-w-[120px]'
      >
        {items.map((item, index) => (
          <DropdownMenuItem
            key={index}
            onClick={item.onClick}
            disabled={item.disabled}
            className='cursor-pointer text-sm font-normal focus:bg-gray-100'
          >
            {item.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default BasicDropDown;
