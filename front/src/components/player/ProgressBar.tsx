'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { toast } from 'sonner';
import { sseManager } from '@/services/connectSSE';
import { SSEEventData } from '@/types/sse';
import BasicToaster from '../common/BasicToaster';

const ProgressBar = ({
  taskId,
  setIsSubmitted,
}: {
  taskId: string;
  setIsSubmitted: (isSubmitted: boolean) => void;
}) => {
  const [contentSeq, setContentSeq] = useState('');
  const [message, setMessage] = useState('');
  const router = useRouter();

  // SSE 연결
  sseManager.connect({
    url: `${process.env.NEXT_PUBLIC_API_BASE_URL}/llm/sse/summary/${taskId}`,
    onConnect: (data) => {
      setMessage(data.message);
    },
    onStart: (data) => {
      setMessage(data.message);
    },
    onProgress: (data) => {
      setMessage(data.message);
    },
    onResult: (data) => {
      setContentSeq(data.contentSeq);
    },
    onComplete: (data) => {
      BasicToaster.success(data.message, {
        id: 'sse youtube',
        persistent: true,
        closeButton: true,
        children: (
          <Link
            href={`/content/${contentSeq}`}
            className='bg-opacity-20 hover:bg-opacity-30 mt-2 rounded-md bg-white px-4 py-2 transition-all'
            onClick={() => toast.dismiss('sse youtube')}
          >
            요약된 내용 보러가기
          </Link>
        ),
      });
    },
    onError: (error: SSEEventData) => {
      BasicToaster.error(error.message, {
        id: 'sse youtube',
        duration: 2000,
      });
      setIsSubmitted(false);
    },
  });

  return (
    <div className='flex flex-col items-center text-lg font-medium text-gray-900'>
      <p>입력하신 영상을 학습 자료로 분석 중이에요</p>
      <p>약 10분 정도 소요될 수 있어요</p>
      <p className='py-2 text-base text-gray-950'>{message}</p>
    </div>
  );
};

export default ProgressBar;
