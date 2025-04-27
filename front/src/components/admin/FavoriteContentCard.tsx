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

const data = {
  labels: ['러닝 챗', 'AI 노트', 'AI 퀴즈', '스냅 리뷰', '링크'],
  datasets: [
    {
      label: '선호 컨텐츠',
      data: [5, 8, 13, 10, 3],
      backgroundColor: ['#B6A4FA', '#A996FD', '#8B7CF6', '#B6A4FA', '#A996FD'],
      borderRadius: 10,
      barPercentage: 0.7,
      categoryPercentage: 0.7,
    },
  ],
};

const options = {
  responsive: true,
  plugins: {
    legend: {
      display: false,
    },
  },
  scales: {
    x: {
      grid: { display: false },
    },
    y: {
      grid: { display: false },
      beginAtZero: true,
    },
  },
};

const FavoriteContentCard = () => (
  <div className='flex h-full w-full items-center justify-center'>
    <Bar data={data} options={options} />
  </div>
);

export default FavoriteContentCard;
