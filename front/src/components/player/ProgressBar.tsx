'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState, useCallback } from 'react';
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
  const [contentSeq, setContentSeq] = useState<string | null>(null);
  const [message, setMessage] = useState('');
  const router = useRouter();

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
        onConnect: handleMessage,
        onStart: handleMessage,
        onProgress: handleMessage,
        onResult: (data) => {
          if (data.contentSeq) {
            currentContentSeq = data.contentSeq;
            setContentSeq(data.contentSeq);
          }
        },
        onComplete: (data) => {
          setIsSubmitted(false);
          if (currentContentSeq) {
            showCompleteToast(data.message, currentContentSeq);
          }
        },
        onError: (error: SSEEventData) => {
          console.error('SSE 에러 발생:', error);
          const errorMessage =
            error.message || '처리 중 오류가 발생했습니다. 다시 시도해주세요.';
          BasicToaster.error(errorMessage, {
            id: 'sse youtube',
            duration: 2000,
          });
          setIsSubmitted(false);
        },
      });
    } catch (error) {
      console.error('SSE 연결 시도 중 에러 발생:', error);
      const errorMessage =
        error instanceof Error
          ? error.message
          : '연결 중 오류가 발생했습니다. 다시 시도해주세요.';
      BasicToaster.error(errorMessage, {
        id: 'sse youtube',
        duration: 2000,
      });
      setIsSubmitted(false);
    }

    return () => {
      setIsSubmitted(false);
      sseManager.disconnect();
    };
  }, [taskId, handleMessage, showCompleteToast]);

  return (
    <div className='flex flex-col items-center text-lg font-medium text-gray-900'>
      <p>입력하신 영상을 학습 자료로 분석 중이에요</p>
      <p>약 10분 정도 소요될 수 있어요</p>
      <p className='py-2 text-base text-gray-950'>{message}</p>
    </div>
  );
};

export default ProgressBar;
