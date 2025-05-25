'use client';

import { ChangeEvent } from 'react';
import Icons from './Icons';

interface SearchBarProps {
  value: string;
  placeholder: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  onSubmit: () => void;
  hasSubmitButton?: boolean;
}

const SearchBar = ({
  value,
  placeholder,
  onChange,
  onSubmit,
  hasSubmitButton = true,
}: SearchBarProps) => {
  return (
    <div className='flex w-full md:w-[600px] lg:w-[800px]'>
      <div className='flex w-full items-center gap-3 rounded border border-gray-200 px-3 py-2 lg:px-4 lg:py-3'>
        <input
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          className='w-full font-medium text-gray-950 focus:outline-none'
        />
        {hasSubmitButton && (
          <button onClick={onSubmit} aria-label='검색버튼'>
            <Icons id='search' />
          </button>
        )}
      </div>
    </div>
  );
};

export default SearchBar;
