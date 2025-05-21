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
  const [progress, setProgress] = useState(0);

  // 완료 토스트 표시 핸들러
  const showCompleteToast = useCallback((message: string, seq: string) => {
    BasicToaster.success(message, {
      id: 'sse youtube',
      persistent: true,
      closeButton: true,
      children: (
        <Link
          href={`/content/${seq}`}
          className='mx-auto mt-2 block w-fit rounded-sm bg-white px-2 py-1 font-bold text-blue-400'
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
        onConnect: () => {
          setProgress(3);
        },
        onStart: () => {
          setProgress(10);
        },
        onProgress: () => {
          setProgress((prev) => Math.min(prev + 5, 95));
        },
        onHeartbeat: () => {
          setProgress((prev) => Math.min(prev + 3, 95));
        },
        onResult: (data) => {
          if (data.contentSeq) {
            currentContentSeq = data.contentSeq;
            setContentSeq(data.contentSeq);
          }
          setProgress(100);
        },
        onComplete: (data) => {
          if (currentContentSeq) {
            showCompleteToast(data.message, currentContentSeq);
          }
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
      setIsSubmitted(false);
      sseManager.disconnect();

      BasicToaster.error(error.message || '연결 중 오류가 발생했습니다.', {
        id: 'sse youtube',
        duration: 2000,
      });
    }

    return () => {
      setIsSubmitted(false);
      sseManager.disconnect();
    };
  }, [taskId, showCompleteToast]);

  return (
    <div className='flex flex-col items-center text-lg font-medium text-gray-900'>
      <p>입력하신 영상을 학습 자료로 분석 중이에요</p>
      <p>최대 10분 정도 소요될 수 있어요</p>
      <div className='flex w-full max-w-md items-center gap-2'>
        <Progress
          value={progress}
          className='[&>div]:animate-shimmer w-full bg-blue-50 text-blue-400 [&>div]:bg-gradient-to-r [&>div]:from-blue-200 [&>div]:via-blue-400 [&>div]:to-blue-200 [&>div]:bg-[length:200%_100%]'
        />
        <span className='min-w-[3rem] text-blue-400'>{progress}%</span>
      </div>
    </div>
  );
};

export default ProgressBar;
