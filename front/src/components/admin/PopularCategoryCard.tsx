import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import React from 'react';
import { Bar } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
);

const dummyData = [
  {
    label: '프로그래밍',
    percent: 55,
    count: 153813,
    color: '#A996FD',
  },
  {
    label: '음악',
    percent: 25,
    count: 153813,
    color: '#FFD36F',
  },
  {
    label: '그림',
    percent: 20,
    count: 153813,
    color: '#7DDCFF',
  },
];

const data = {
  labels: dummyData.map((d) => `${d.label} ${d.percent}%`),
  datasets: [
    {
      data: dummyData.map((d) => d.percent),
      backgroundColor: dummyData.map((d) => d.color),
      borderRadius: 20,
      barPercentage: 0.7,
      categoryPercentage: 0.7,
    },
  ],
};

const options = {
  indexAxis: 'y' as const,
  responsive: true,
  scales: {
    x: {
      display: false,
      grid: {
        display: false,
      },
    },
    y: {
      grid: {
        display: false,
      },
    },
  },
  plugins: {
    legend: {
      display: false,
    },
  },
};

const PopularCategoryCard = () => {
  return (
    <div className='w-full rounded-2xl bg-white p-6'>
      <div className='flex items-center justify-between gap-8'>
        <div className='flex-1'>
          {dummyData.map((d, i) => (
            <div key={d.label} className='mb-3 flex items-center'>
              <span
                className='mr-2.5 h-3.5 w-3.5 rounded-full'
                style={{ backgroundColor: d.color }}
              />
              <span className='mr-2 text-lg text-gray-600'>
                {d.label} {d.percent}%
              </span>
              <span className='text-base text-gray-400'>
                {d.count.toLocaleString()} 명
              </span>
            </div>
          ))}
        </div>
        <div className='h-full flex-1'>
          <Bar data={data} options={options} height={50} />
        </div>
      </div>
    </div>
  );
};

export default PopularCategoryCard;
