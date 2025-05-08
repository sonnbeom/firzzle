'use client';

import { useState } from 'react';
import { DateRange } from 'react-date-range';
import { ko } from 'date-fns/locale';
import { format } from 'date-fns';
import 'react-date-range/dist/styles.css';
import 'react-date-range/dist/theme/default.css';

interface DateRangeSelectorProps {
  onChange?: (ranges: { startDate: Date; endDate: Date }) => void;
}

const DateRangeSelector = ({ onChange }: DateRangeSelectorProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [state, setState] = useState([
    {
      startDate: new Date(),
      endDate: new Date(),
      key: 'selection',
    },
  ]);

  const handleSelect = (item: any) => {
    setState([item.selection]);
    onChange?.({
      startDate: item.selection.startDate,
      endDate: item.selection.endDate
    });
    setIsOpen(false);
  };

  const toggleCalendar = () => {
    setIsOpen(!isOpen);
  };

  const formatDateRange = () => {
    const startDate = format(state[0].startDate, 'yyyy.MM.dd');
    const endDate = format(state[0].endDate, 'yyyy.MM.dd');
    return `${startDate} - ${endDate}`;
  };

  return (
    <div className="relative">
      <div 
        className="flex items-center gap-2 cursor-pointer" 
        onClick={toggleCalendar}
      >
        <span className="font-medium">조회 기간</span>
        <div className="flex items-center gap-2 rounded-md border px-4 py-2 bg-white">
          <span>{formatDateRange()}</span>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            strokeWidth={1.5}
            stroke="currentColor"
            className="w-5 h-5"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5"
            />
          </svg>
        </div>
      </div>
      {isOpen && (
        <div className="absolute top-full left-0 mt-2 z-10 shadow-lg rounded-lg bg-white border">
          <DateRange
            editableDateInputs={true}
            onChange={handleSelect}
            moveRangeOnFirstSelection={false}
            ranges={state}
            months={1}
            direction="horizontal"
            locale={ko}
            dateDisplayFormat="yyyy.MM.dd"
          />
        </div>
      )}
    </div>
  );
};

export default DateRangeSelector;
