'use client';

import Image from 'next/image';
import React, { useState } from 'react';
import Icons from '../common/Icons';
import { Button } from '../ui/button';
import Share from './Share';

interface ReviewImage {
  src: string;
  title: string;
  description: string;
}

const mockImages: ReviewImage[] = [
  {
    src: '/assets/images/Firzzle.png',
    title: 'AI vs Machine Learning vs Deep Learning',
    description: '인공지능의 세부 분야와 차이점을 알아봅시다.',
  },
  {
    src: '/assets/images/Firzzle.png',
    title: '머신러닝의 정의',
    description:
      '비지도 학습의 특징과 클러스터링에 대해 알아봅시다비지도 학습의 특징과 클러스터링에 대해 알아봅시다비지도 학습의 특징과 클러스터링에 대해 알아봅시다비지도 학습의 특징과 클러스터링에 대해 알아봅시다비지도 학습의 특징과 클러스터링에 대해 알아봅시다.',
  },
  {
    src: '/assets/images/Firzzle.png',
    title: 'Reinforcement Learning',
    description: '강화학습의 기본 개념과 피드백 학습 방식을 소개합니다.',
  },
  {
    src: '/assets/images/Firzzle.png',
    title: 'Unsupervised Learning',
    description: '강화학습의 기본 개념과 피드백 학습 방식을 소개합니다.',
  },
];

const Review = () => {
  const [isShareOpen, setIsShareOpen] = useState(false);
  return (
    <div className='space-y-6'>
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Icons id='arrow-left' className='h-6 w-6' />
          <h1 className='text-sm font-semibold text-gray-950 sm:text-lg'>
            AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리
          </h1>
        </div>
        <Button
          variant='default'
          className='text-xs sm:text-lg'
          onClick={() => setIsShareOpen(true)}
        >
          공유하기
        </Button>
        <Share isOpen={isShareOpen} onClose={() => setIsShareOpen(false)} />
      </div>
      <div className='mx-auto max-w-7xl rounded-lg bg-blue-50 p-6'>
        <div className='grid grid-cols-1 gap-4 sm:grid-cols-2'>
          {mockImages.map((image, index) => (
            <div
              key={index}
              className='group relative aspect-video overflow-hidden'
            >
              <Image
                src={image.src}
                alt={image.title}
                fill
                className='object-cover transition-transform duration-300 group-hover:scale-110'
              />
              <div className='absolute inset-0 flex flex-col justify-center bg-black/60 p-4 opacity-0 transition-opacity duration-300 group-hover:opacity-100'>
                <p className='sm:text-md text-center text-sm font-semibold text-white'>
                  {image.description}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className='py-4 text-center'>
          <h2 className='text-md mt-6 font-semibold text-gray-950 sm:text-xl'>
            AI, 딥러닝, 머신러닝, 초간단 인공지능 개념정리
          </h2>
          <p className='sm:text-md mt-4 mb-6 text-sm text-gray-600'>
            2025.04.23
          </p>
        </div>
      </div>
    </div>
  );
};

export default Review;
