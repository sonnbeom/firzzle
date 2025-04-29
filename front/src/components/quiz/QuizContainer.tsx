'use client';

import { useRouter } from 'next/navigation';
import React, {
  useState,
  useCallback,
  useMemo,
  useEffect,
  useRef,
} from 'react';

// components
import BasicDialog from '@/components/common/BasicDialog';
import { Button } from '@/components/ui/button';
import QuizAnswer from './QuizAnswer';
import QuizCard from './QuizCard';

declare global {
  interface Window {
    __reloadRequested: boolean;
  }
}

interface QuizContent {
  quizNo: string;
  question: string;
  answer: boolean;
  description: string;
  timestamp: number;
}

interface QuizContainerProps {
  quizContents: QuizContent[];
}

// 페이지 이동 타입 정의
type NavigationType = 'push' | 'back';

// 페이지 이동 시도 정보 저장 타입
interface PendingNavigationInfo {
  type: NavigationType;
  url?: string;
  options?: object;
}

const QuizContainer = ({ quizContents }: QuizContainerProps) => {
  const router = useRouter();

  // 퀴즈 답안 상태
  const [selected, setSelected] = useState<Array<'O' | 'X' | null>>(() =>
    new Array(quizContents.length).fill(null),
  );
  const [showAnswer, setShowAnswer] = useState(false);

  // 정답 선택 처리 함수
  const handleSelect = useCallback((index: number, value: 'O' | 'X') => {
    setSelected((prev) => {
      const newSelected = [...prev];
      newSelected[index] = value;
      return newSelected;
    });
  }, []);

  // 모든 문제에 답안을 선택했는지 여부
  const isCompleted = useMemo(
    () => selected.every((value) => value !== null),
    [selected],
  );

  // 페이지 이탈 방지 관련 상태
  const [showDialog, setShowDialog] = useState(false);
  const blockUnload = useRef(false);
  const originalPushRef = useRef<
    ((href: string, options?: object) => Promise<boolean>) | null
  >(null);
  // 대기 중인 이동 정보
  const [pendingNavigation, setPendingNavigation] =
    useState<PendingNavigationInfo | null>(null);

  // 뒤로가기 감지 및 처리 함수
  const handlePopState = useCallback(
    (e: PopStateEvent) => {
      const hasAnswered = selected.some((value) => value !== null);
      if (hasAnswered && !showAnswer) {
        window.history.pushState(null, '', window.location.href);

        setPendingNavigation({ type: 'back' });
        setShowDialog(true);
      }
    },
    [selected, showAnswer],
  );

  useEffect(() => {
    const hasAnswered = selected.some((value) => value !== null);
    if (!hasAnswered || showAnswer) {
      blockUnload.current = false;
      return;
    }

    blockUnload.current = true;

    // 원래 라우터 함수 참조
    const originalPush = router.push.bind(router);
    originalPushRef.current = originalPush;

    // 라우터 오버라이드
    router.push = (href: string, options?: object) => {
      setPendingNavigation({
        type: 'push',
        url: href,
        options,
      });
      setShowDialog(true);
      return Promise.resolve(false);
    };

    window.addEventListener('popstate', handlePopState);
    window.history.pushState(null, '', window.location.href);

    return () => {
      window.removeEventListener('popstate', handlePopState);

      if (originalPushRef.current) router.push = originalPushRef.current;
      blockUnload.current = false;
    };
  }, [selected, router, showAnswer, handlePopState]);

  // 경고 다이얼로그 상태 관리
  const handleDialogChange = (open: boolean) => {
    setShowDialog(open);
    if (!open) {
      setPendingNavigation(null);
    }
  };

  // 실제 페이지 이동 수행 함수
  const executeNavigation = () => {
    if (!pendingNavigation) return;

    // 페이지 이탈 방지 해제
    blockUnload.current = false;

    try {
      // 다른 페이지로 이동
      if (pendingNavigation.type === 'push') {
        if (originalPushRef.current && pendingNavigation.url) {
          const pushFn = originalPushRef.current;
          pushFn(pendingNavigation.url, pendingNavigation.options);
        }
      } else if (pendingNavigation.type === 'back') {
        // 뒤로가기
        window.history.back();
      }
    } catch (error) {
      console.error('Navigation failed:', error);
    }
  };

  return (
    <div className='relative min-h-screen w-full px-2 md:px-10'>
      {/* Dialog */}
      <BasicDialog
        isOpen={showDialog}
        onOpenChange={handleDialogChange}
        title='주의'
        description='현재 화면을 벗어나면 문제는 초기화됩니다. 계속하시겠습니까?'
      >
        <Button variant='ghost' onClick={() => handleDialogChange(false)}>
          취소
        </Button>
        <Button
          onClick={() => {
            setShowDialog(false);
            setPendingNavigation((prev) => {
              // executeNavigation 함수 setTimeout으로 실행
              setTimeout(() => {
                blockUnload.current = false;

                const navType = prev?.type;
                if (
                  navType === 'push' &&
                  prev?.url &&
                  originalPushRef.current
                ) {
                  const pushFn = originalPushRef.current;
                  pushFn(prev.url, prev.options);
                } else if (navType === 'back') {
                  window.history.back();
                }
              }, 10);
              return null;
            });
          }}
        >
          확인
        </Button>
      </BasicDialog>
      {/* Quiz */}
      <div className='space-y-6 pb-28'>
        {quizContents.map((quiz, index) =>
          showAnswer ? (
            <QuizAnswer
              key={quiz.quizNo}
              quizNo={quiz.quizNo}
              question={quiz.question}
              answer={selected[index] === 'O' ? quiz.answer : !quiz.answer}
              description={quiz.description}
              timestamp={quiz.timestamp}
            />
          ) : (
            <QuizCard
              key={quiz.quizNo}
              quizNo={quiz.quizNo}
              question={quiz.question}
              selected={selected[index]}
              onSelect={(value) => handleSelect(index, value)}
            />
          ),
        )}
      </div>
      {/* 도전하기 버튼 */}
      {!showAnswer && (
        <div className='bottom-0 left-0 w-full bg-white py-4'>
          <Button
            variant={isCompleted ? 'default' : 'disabled'}
            className={`w-full py-6 text-lg font-semibold ${isCompleted ? 'bg-blue-400 hover:bg-blue-400' : ''} text-white`}
            onClick={() => {
              if (isCompleted) {
                window.scrollTo(0, 0);
                setShowAnswer(true);
              }
            }}
          >
            도전하기
          </Button>
        </div>
      )}
    </div>
  );
};

export default QuizContainer;
