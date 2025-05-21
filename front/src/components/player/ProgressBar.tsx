'use client';

import Link from 'next/link';
import { useEffect, useState, useCallback } from 'react';
import { toast } from 'sonner';
import { Progress } from '@/components/ui/progress';
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
  const [contentSeq, setContentSeq] = useState<string | null>(null);
  const [message, setMessage] = useState('');
  const [progress, setProgress] = useState(0);

  // 메시지 업데이트 핸들러
  const handleMessage = useCallback((data: SSEEventData) => {
    if (data.message) {
      setMessage(data.message);
    }
  }, []);

  // 완료 토스트 표시 핸들러
  const showCompleteToast = useCallback((message: string, seq: string) => {
    BasicToaster.success(message, {
      id: 'sse youtube',
      persistent: true,
      closeButton: true,
      children: (
        <Link
          href={`/content/${seq}`}
          className='border-b border-white px-2 font-semibold text-white'
          onClick={() => toast.dismiss('sse youtube')}
        >
          요약 보러가기
        </Link>
      ),
    });
  }, []);

  useEffect(() => {
    if (!taskId || !setIsSubmitted) return;

    let currentContentSeq: string | null = null;

    console.log('SSE 연결 시작');

    // SSE 연결
    try {
      sseManager.connect({
        url: `${process.env.NEXT_PUBLIC_API_BASE_URL}/llm/sse/summary/${taskId}`,
        onConnect: (data) => {
          handleMessage(data);
          setProgress(3);
        },
        onStart: (data) => {
          handleMessage(data);
          setProgress(10);
        },
        onProgress: (data) => {
          handleMessage(data);
          setProgress((prev) => Math.min(prev + 5, 95));
        },
        onHeartbeat: () => {
          setProgress((prev) => Math.min(prev + 1, 95));
        },
        onResult: (data) => {
          if (data.contentSeq) {
            currentContentSeq = data.contentSeq;
            setContentSeq(data.contentSeq);
          }
          setProgress(85);
        },
        onComplete: (data) => {
          if (currentContentSeq) {
            showCompleteToast(data.message, currentContentSeq);
          }
          setProgress(100);
          setIsSubmitted(false);
          sseManager.disconnect();
        },
        onError: (error: SSEEventData | Event) => {
          console.error('SSE 에러 발생:', error);
          if (error instanceof Event) {
            BasicToaster.error(error.type, {
              id: 'sse youtube',
              duration: 2000,
            });
          } else {
            BasicToaster.error(
              error.message || '처리 중 오류가 발생했습니다.',
              {
                id: 'sse youtube',
                duration: 2000,
              },
            );
          }
          setIsSubmitted(false);
          sseManager.disconnect();
        },
      });
    } catch (error) {
      console.error('SSE 연결 시도 중 에러 발생:', error);
      BasicToaster.error(error.message || '연결 중 오류가 발생했습니다.', {
        id: 'sse youtube',
        duration: 2000,
      });
      setIsSubmitted(false);
      sseManager.disconnect();
    }

    return () => {
      setIsSubmitted(false);
      sseManager.disconnect();
    };
  }, [taskId, handleMessage, showCompleteToast]);

  return (
    <div className='flex flex-col items-center text-lg font-medium text-gray-900'>
      <p>입력하신 영상을 학습 자료로 분석 중이에요</p>
      <p className='py-2 text-base text-gray-950'>{message}</p>
      <Progress value={progress} className='w-full max-w-md' />
    </div>
  );
};

export default ProgressBar;
