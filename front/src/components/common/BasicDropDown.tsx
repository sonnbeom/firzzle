'use client';

import * as React from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import Icons from '../common/Icons';

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

const BasicDropDown = ({
  items,
  defaultValue,
  onChange,
}: BasicDropDownProps) => {
  const [selectedValue, setSelectedValue] = React.useState(
    defaultValue || items[0].value,
  );

  const selectedItem =
    items.find((item) => item.value === selectedValue) || items[0];

  const handleSelect = (item: DropDownItem) => {
    setSelectedValue(item.value);
    onChange?.(item.value);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button className='flex h-8 w-[120px] items-center justify-between gap-2 rounded border border-gray-200 bg-white px-3 text-sm hover:bg-gray-50'>
          <span className='truncate'>{selectedItem.label}</span>
          <Icons id='arrow-down' size={16} color='text-gray-500' />
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
