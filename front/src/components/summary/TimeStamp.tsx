'use client';

interface TimeStampProps {
  time: string;
}

const TimeStamp = ({ time }: TimeStampProps) => {
  return <button className='text-gray-700 hover:text-blue-300'>{time}</button>;
};

export default TimeStamp;
