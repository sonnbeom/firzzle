'use client';

import { format } from 'date-fns';
import { ko } from 'date-fns/locale';
import { useState, useEffect } from 'react';
import { DateRange, Range, RangeKeyDict } from 'react-date-range';
import 'react-date-range/dist/styles.css';
import 'react-date-range/dist/theme/default.css';
import { DateRangeSelectorProps } from '@/types/chart';
import { formatToLocalDate } from '@/utils/formatDate';

const DateRangeSelector = ({
  onChange,
  initialStartDate,
  initialEndDate,
}: DateRangeSelectorProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [dateRange, setDateRange] = useState({
    startDate: initialStartDate || new Date(),
    endDate: initialEndDate || new Date(),
  });
  const [tempRange, setTempRange] = useState(dateRange);

  useEffect(() => {
    setDateRange({
      startDate: initialStartDate || new Date(),
      endDate: initialEndDate || new Date(),
    });
  }, [initialStartDate, initialEndDate]);

  const handleSelect = (ranges: RangeKeyDict) => {
    const selection = ranges.selection as Range;
    if (selection.startDate && selection.endDate) {
      setTempRange({
        startDate: selection.startDate,
        endDate: selection.endDate,
      });
    }
  };

  const handleConfirm = () => {
    setDateRange(tempRange);
    setIsOpen(false);
    // 💡 확정 시점에만 onChange 호출
    onChange?.({
      ...tempRange,
      formattedStart: formatToLocalDate(tempRange.startDate),
      formattedEnd: formatToLocalDate(tempRange.endDate),
    });
  };

  const handleCancel = () => {
    setTempRange(dateRange);
    setIsOpen(false);
  };

  const toggleCalendar = () => {
    if (!isOpen) setTempRange(dateRange);
    setIsOpen(!isOpen);
  };

  const formatDateRange = () => {
    const start = format(dateRange.startDate, 'yyyy.MM.dd');
    const end = format(dateRange.endDate, 'yyyy.MM.dd');
    return `${start} - ${end}`;
  };

  return (
    <div className='relative'>
      <div
        className='flex cursor-pointer items-center gap-2'
        onClick={toggleCalendar}
      >
        <span className='font-medium'>조회 기간</span>
        <div className='flex items-center gap-2 rounded-md border bg-white px-4 py-2'>
          <span>{formatDateRange()}</span>
          <svg
            xmlns='http://www.w3.org/2000/svg'
            fill='none'
            viewBox='0 0 24 24'
            strokeWidth={1.5}
            stroke='currentColor'
            className='h-5 w-5'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              d='M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5'
            />
          </svg>
        </div>
      </div>
      {isOpen && (
        <div className='absolute top-full left-0 z-10 mt-2 rounded-lg border bg-white shadow-lg'>
          <DateRange
            editableDateInputs={true}
            onChange={handleSelect}
            moveRangeOnFirstSelection={false}
            ranges={[{ ...tempRange, key: 'selection' }]}
            months={1}
            direction='horizontal'
            locale={ko}
            maxDate={new Date()}
            dateDisplayFormat='yyyy.MM.dd'
          />
          <div className='flex justify-end gap-2 border-t p-3'>
            <button
              onClick={handleCancel}
              className='rounded px-3 py-1 text-sm text-gray-500 hover:bg-gray-100'
            >
              취소
            </button>
            <button
              onClick={handleConfirm}
              className='rounded bg-blue-500 px-3 py-1 text-sm text-white hover:bg-blue-600'
            >
              확인
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default DateRangeSelector;
