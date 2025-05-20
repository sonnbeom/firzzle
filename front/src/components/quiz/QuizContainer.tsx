'use client';

import { useState, useEffect } from 'react';
import { getQuiz } from '@/api/quiz';
import BasicDialog from '@/components/common/BasicDialog';
import { Button } from '@/components/ui/button';
import { usePreventNavigation } from '@/hooks/usePreventNavigation';
import { useQuiz } from '@/hooks/useQuiz';
import { QuizData } from '@/types/quiz';
import QuizAnswer from './QuizAnswer';
import QuizCard from './QuizCard';

interface QuizContainerProps {
  contentSeq: string;
}

const QuizContainer = ({ contentSeq }: QuizContainerProps) => {
  const [quizData, setQuizData] = useState<QuizData>();

  useEffect(() => {
    const fetchQuizData = async () => {
      const data = await getQuiz(contentSeq);
      setQuizData(data.data);
    };
    fetchQuizData();
  }, [contentSeq]);

  // useQuiz 훅으로 퀴즈 관련 로직 분리
  const {
    selected,
    showAnswer,
    isCompleted,
    hasAnswered,
    handleSelect,
    handleSubmit,
    getAnswerProps,
    getCardProps,
  } = useQuiz(quizData, contentSeq);

  // usePreventNavigation 훅 사용
  const {
    showDialog: showNavigationDialog,
    setShowDialog: setNavigationDialog,
    confirmNavigation,
  } = usePreventNavigation(hasAnswered && !showAnswer);

  return (
    <div className='relative w-full px-2 md:px-10'>
      {/* Navigation Dialog */}
      <BasicDialog
        isOpen={showNavigationDialog}
        onOpenChange={setNavigationDialog}
        title='주의'
        description='현재 화면을 벗어나면 문제는 초기화됩니다. 계속하시겠습니까?'
      >
        <Button onClick={confirmNavigation}>확인</Button>
      </BasicDialog>
      {/* Quiz */}
      <div className='space-y-6 pb-28'>
        {!quizData ? (
          <div>로딩 중...</div>
        ) : (
          quizData.content.questions.map((question, index) => {
            if (showAnswer) {
              return (
                <QuizAnswer
                  key={question.questionSeq}
                  {...getAnswerProps(question, index)}
                />
              );
            }
            return (
              <QuizCard
                key={question.questionSeq}
                {...getCardProps(question, index, selected, handleSelect)}
              />
            );
          })
        )}
      </div>

      {/* 도전하기 버튼 */}
      {!showAnswer && (
        <div className='sticky bottom-0 left-0 w-full bg-white py-3'>
          <Button
            variant={isCompleted ? 'default' : 'disabled'}
            className={`w-full py-4 text-lg font-semibold ${isCompleted ? 'bg-blue-400 hover:bg-blue-400' : ''} text-white`}
            onClick={handleSubmit}
            disabled={!isCompleted}
          >
            도전하기
          </Button>
        </div>
      )}
    </div>
  );
};

export default QuizContainer;
