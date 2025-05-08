'use client';

import DateRangeSelector from '@/components/admin/DateRangeSelector';

const StrategyBoardPage = () => {
    const handleDateChange = ({ startDate, endDate }: { startDate: Date; endDate: Date }) => {
        // 추후 여기서 데이터 fetching 등을 수행
      };

    return(    <div className='flex flex-col gap-6 p-6'> <DateRangeSelector onChange={handleDateChange} /></div>);
    
};

export default StrategyBoardPage;
