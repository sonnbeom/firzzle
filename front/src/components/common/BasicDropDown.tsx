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
  value: string;
  disabled?: boolean;
}

interface BasicDropDownProps {
  items: DropDownItem[];
  defaultValue?: string;
  onChange?: (value: string) => void;
}

const BasicDropDown = ({ items, defaultValue, onChange }: BasicDropDownProps) => {
  const [selectedValue, setSelectedValue] = React.useState(defaultValue || items[0].value);
  
  const selectedItem = items.find(item => item.value === selectedValue) || items[0];

  const handleSelect = (item: DropDownItem) => {
    setSelectedValue(item.value);
    onChange?.(item.value);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button className='flex h-8 w-[120px] items-center justify-between gap-2 rounded border border-gray-200 bg-white px-3 text-sm hover:bg-gray-50'>
          <span className='truncate'>{selectedItem.label}</span>
          <svg className='h-4 w-4 shrink-0 text-gray-500' fill='none' viewBox='0 0 24 24' stroke='currentColor'>
            <path strokeLinecap='round' strokeLinejoin='round' strokeWidth={2} d='M19 9l-7 7-7-7' />
          </svg>
        </button>
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align='center'
        side='bottom'
        className='min-w-[120px]'
      >
        {items.map((item) => (
          <DropdownMenuItem
            key={item.value}
            onClick={() => handleSelect(item)}
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
