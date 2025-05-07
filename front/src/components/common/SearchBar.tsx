'use client';

import Icons from './Icons';

const SearchBar = () => {
  return (
    <div className='flex w-full md:w-[600px] xl:w-[800px]'>
      <div className='flex w-full items-center gap-3 rounded border border-gray-200 px-3 py-2 xl:px-4 xl:py-3'>
        <input
          value={''}
          onChange={() => {}}
          placeholder='검색어를 입력하세요.'
          className='w-full font-medium text-gray-950 focus:outline-none'
        />
        <button>
          <Icons id='search' />
        </button>
      </div>
    </div>
  );
};

export default SearchBar;
