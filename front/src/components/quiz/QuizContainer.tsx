'use client';

import { useState, useCallback, useMemo } from 'react';
import { submitQuizAnswers } from '@/api/quiz';
import BasicDialog from '@/components/common/BasicDialog';
import { Button } from '@/components/ui/button';
import { usePreventNavigation } from '@/hooks/usePreventNavigation';
import { QuizData, QuizSubmitRequest, QuizSubmitResponse } from '@/types/quiz';
import QuizAnswer from './QuizAnswer';
import QuizCard from './QuizCard';

declare global {
  interface Window {
    __reloadRequested: boolean;
  }
}

interface QuizContainerProps {
  quizContents: QuizData[];
  contentId: string;
}

const QuizContainer = ({ quizContents, contentId }: QuizContainerProps) => {
  const quizData = quizContents;
  // 퀴즈 답안 상태
  const [selected, setSelected] = useState<Array<'O' | 'X' | null>>(() =>
    new Array(quizData.length).fill(null),
  );
  const [showAnswer, setShowAnswer] = useState(false);
  const [quizResult, setQuizResult] = useState<QuizSubmitResponse | null>(null);

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

  // 페이지 이탈 방지 로직
  const hasAnswered = selected.some((value) => value !== null);
  const {
    showDialog,
    setShowDialog,
    pendingNavigation,
    setPendingNavigation,
    blockUnload,
    router,
    originalPushRef,
  } = usePreventNavigation(hasAnswered && !showAnswer);

  // 퀴즈 제출 처리
  const handleSubmit = async () => {
    if (!isCompleted) return;

    const request: QuizSubmitRequest = {
      answers: quizData.map((quiz, index) => ({
        quizNo: quiz.quizNo,
        answer: selected[index] === 'O',
      })),
    };

    try {
      const response = await submitQuizAnswers(contentId, request);
      setQuizResult(response.data);
      window.scrollTo(0, 0);
      setShowAnswer(true);
    } catch (error) {
      console.error('Error submitting quiz:', error);
    }
  };

  return (
    <div className='relative min-h-screen w-full px-2 md:px-10'>
      {/* Dialog */}
      <BasicDialog
        isOpen={showDialog}
        onOpenChange={setShowDialog}
        title='주의'
        description='현재 화면을 벗어나면 문제는 초기화됩니다. 계속하시겠습니까?'
      >
        <Button variant='ghost' onClick={() => setShowDialog(false)}>
          취소
        </Button>
        <Button
          onClick={() => {
            setShowDialog(false);
            setPendingNavigation((prev) => {
              setTimeout(() => {
                blockUnload.current = false;
                if (
                  prev?.type === 'push' &&
                  prev.url &&
                  originalPushRef.current
                ) {
                  const pushFn = originalPushRef.current;
                  pushFn(prev.url, prev.options);
                } else if (prev?.type === 'back') {
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
        {quizData.map((quiz, index) =>
          showAnswer ? (
            <QuizAnswer
              key={quiz.quizNo}
              quizNo={quiz.quizNo}
              question={quiz.question}
              answer={quiz.answer}
              correct={selected[index] === 'O' ? quiz.answer : !quiz.answer}
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
            onClick={handleSubmit}
          >
            도전하기
          </Button>
        </div>
      )}
    </div>
  );
};

export default QuizContainer;
