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
  x: string; // x축 날짜
  y: number; // y축 값
}

interface DataSet {
  label: string;
  data: DataPoint[];
}

interface CurveGraphCardProps {
  title?: string;
  description?: string;
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

const CurveGraphCard = ({
  title,
  description,
  tags,
  dataSets,
  startDate,
  endDate,
  mode,
}: CurveGraphCardProps) => {
  const predefinedColors = [
    '#4A90E2', // 하늘색
    '#50C878', // 에메랄드
    '#FF6B6B', // 코랄
    '#845EF7', // 보라
    '#FF922B', // 주황
    '#20C997', // 민트
    '#F06595', // 분홍
    '#7950F2', // 진보라
    '#94D82D', // 라임
    '#FF8787', // 연분홍
  ];

  let colorIndex = 0;
  const generateRandomColor = () => {
    const color = predefinedColors[colorIndex];
    colorIndex = (colorIndex + 1) % predefinedColors.length;
    return color;
  };

  const chartData = {
    labels: dataSets[0].data.map((point) => point.x),
    datasets: dataSets.map((set) => {
      const color = generateRandomColor();
      return {
        label: set.label,
        data: set.data.map((point) => point.y),
        borderColor: color,
        backgroundColor: color,
        tension: 0.4,
        borderWidth: 2,
        pointRadius: 4,
        pointBorderWidth: 5,
        pointBorderColor: `${color}80`,
        pointHoverRadius: 3,
      };
    }),
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        align: 'center' as const,
        labels: {
          boxWidth: 8,
          boxHeight: 8,
          usePointStyle: true,
          pointStyle: 'circle',
          padding: 15,
          itemSpacing: 30,
          font: {
            size: 14,
          },
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
    <div className='h-[300px] w-full rounded-lg bg-white p-4 shadow-sm lg:rounded-2xl lg:p-6'>
      {(title || tags?.length || mode || description) && (
        <div className='mb-3 flex flex-wrap items-center justify-between gap-2 lg:mb-4'>
          <div className='flex flex-wrap items-center gap-2'>
            {tags?.map((tag, index) => (
              <BasicBadge key={index} text={tag.text} color={tag.color} />
            ))}
            {title && <span className='text-base font-medium'>{title}</span>}
            {description && (
              <BasicPopOver
                trigger={
                  <InfoCircledIcon className='h-4 w-4 cursor-pointer text-gray-400' />
                }
                content={
                  <span className='text-xs text-gray-600'>{description}</span>
                }
              />
            )}
          </div>
        </div>
      )}
      <div className='h-[calc(100%-3rem)] w-full'>
        <div
          className={`graph-scroll ${dataSets[0]?.data.length > 10 ? 'overflow-x-auto' : ''}`}
        >
          <div
            className={
              dataSets[0]?.data.length > 10 ? 'min-w-[1200px]' : 'w-full'
            }
          >
            <Line data={chartData} options={options} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default CurveGraphCard;
