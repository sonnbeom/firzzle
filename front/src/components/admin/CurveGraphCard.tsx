'use client';

import { InfoCircledIcon } from '@radix-ui/react-icons';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

import BasicBadge from '@/components/common/BasicBadge';
import BasicPopOver from '@/components/common/BasicPopOver';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
);

interface DataPoint {
  x: string;
  y: number;
}

interface DataSet {
  label: string;
  data: DataPoint[];
}

const generateRandomColor = () => {
  const colors = [
    '#FF6B6B',
    '#4ECDC4',
    '#45B7D1',
    '#96CEB4',
    '#FFEEAD',
    '#D4A5A5',
    '#9B6B6B',
    '#E9967A',
    '#4682B4',
    '#6B8E23',
    '#B19CD9',
    '#FFB6C1',
    '#20B2AA',
    '#F0E68C',
    '#DDA0DD',
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

interface CurveGraphCardProps {
  title: string;
  description: string;
  tags?: {
    text: string;
    color: string;
  }[];
  dataSets: DataSet[];
  startDate: Date;
  endDate: Date;
  mode?: {
    text: string;
    color: string;
  };
}

const formatDate = (date: Date, interval: 'day' | 'month' | 'year'): string => {
  switch (interval) {
    case 'day':
      return date.toLocaleDateString('ko-KR', {
        month: '2-digit',
        day: '2-digit',
      });
    case 'month':
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
      });
    case 'year':
      return date.getFullYear().toString();
  }
};

const getDateInterval = (
  startDate: Date,
  endDate: Date,
): 'day' | 'month' | 'year' => {
  const diffInDays = Math.floor(
    (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24),
  );
  if (diffInDays <= 31) return 'day';
  if (diffInDays <= 365) return 'month';
  return 'year';
};

const CurveGraphCard = ({
  title,
  description,
  tags,
  dataSets,
  startDate,
  endDate,
  mode,
}: CurveGraphCardProps) => {
  const interval = getDateInterval(startDate, endDate);

  const chartData = {
    labels: dataSets[0].data.map((point) => point.x),
    datasets: dataSets.map((set) => {
      const color = generateRandomColor();
      return {
        label: set.label,
        data: set.data.map((point) => point.y),
        borderColor: color,
        backgroundColor: `${color}20`,
        tension: 0.4,
        pointRadius: 4,
        pointBackgroundColor: color,
        fill: false,
      };
    }),
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          usePointStyle: true,
          padding: 20,
        },
      },
    },
    scales: {
      x: {
        grid: {
          display: true,
          drawBorder: false,
          drawOnChartArea: true,
          drawTicks: true,
          borderDash: [5, 5],
        },
      },
      y: {
        grid: {
          display: true,
          drawBorder: false,
          drawOnChartArea: true,
          drawTicks: true,
          borderDash: [5, 5],
        },
        min: 0,
      },
    },
  };

  return (
    <div className='h-[300px] w-full rounded-2xl bg-white p-4 shadow-sm lg:h-[350px] lg:p-6'>
      <div className='mb-4 flex flex-wrap items-center justify-between gap-2 lg:mb-6'>
        <div className='flex flex-wrap items-center gap-2'>
          {tags?.map((tag, index) => (
            <BasicBadge key={index} text={tag.text} color={tag.color} />
          ))}
          <span className='text-base font-medium lg:text-lg'>{title}</span>
          <BasicPopOver
            trigger={
              <InfoCircledIcon className='h-3.5 w-3.5 cursor-pointer text-gray-400 lg:h-5 lg:w-5' />
            }
            content={
              <span className='text-xs text-gray-600 lg:text-sm'>
                {description}
              </span>
            }
          />
        </div>
        {mode && <BasicBadge text={mode.text} color={mode.color} />}
      </div>
      <div className='h-[220px] lg:h-[250px]'>
        <Line data={chartData} options={options} />
      </div>
    </div>
  );
};

export default CurveGraphCard;
