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
  labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  datasets: [
    {
      label: '프로그래밍',
      data: [12, 19, 10, 15, 18, 20, 17],
      backgroundColor: '#A996FD',
      borderRadius: 10,
      barPercentage: 0.7,
      categoryPercentage: 0.7,
    },
    {
      label: '음악',
      data: [8, 11, 7, 10, 12, 13, 11],
      backgroundColor: '#FFD36F',
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

const VideoLengthCard = () => (
  <div className='flex h-full w-full items-center justify-center'>
    <Bar data={data} options={options} />
  </div>
);

export default VideoLengthCard;
