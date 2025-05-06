'use client';

import { useState, useCallback, useMemo } from 'react';
import { submitQuizAnswers } from '@/api/quiz';
import BasicDialog from '@/components/common/BasicDialog';
import { Button } from '@/components/ui/button';
import { usePreventNavigation } from '@/hooks/usePreventNavigation';
import {
  QuizData,
  QuizSubmitRequest,
  QuizSubmitResponse,
  QuizOption,
  QuizCardProps,
  QuizAnswerProps,
} from '@/types/quiz';
import QuizAnswer from './QuizAnswer';
import QuizCard from './QuizCard';

declare global {
  interface Window {
    __reloadRequested: boolean;
  }
}

interface QuizContainerProps {
  quizData: QuizData;
  contentSeq: string;
}

const getAnswerProps = (
  question: QuizData['questions'][0],
  index: number,
  questionResults?: QuizSubmitResponse['questionResults'],
): QuizAnswerProps => {
  // API 응답의 explanation 찾기
  const explanation = questionResults?.find(
    (result) => result.questionSeq === question.questionSeq
  )?.explanation || '';

  return {
    questionSeq: index + 1,
    text: question.text,
    correct: question.userAnswer?.isCorrect || false,
    explanation: explanation,  // API의 explanation 사용
    timestamp: question.timestamp,
    selectedOption: question.options.find(
      (opt): opt is QuizOption =>
        opt.optionSeq === question.userAnswer?.selectedOptionSeq,
    ),
    correctOption: question.userAnswer?.isCorrect
      ? question.options.find(
          (opt): opt is QuizOption =>
            opt.optionSeq === question.userAnswer?.selectedOptionSeq,
        )
      : undefined,
  };
};

const getCardProps = (
  question: QuizData['questions'][0],
  index: number,
  selected: Array<number | null>,
  handleSelect: (index: number, optionSeq: number) => void,
): QuizCardProps => {
  return {
    questionSeq: index + 1,
    text: question.text,
    options: question.options,
    selected: selected[index],
    onSelect: (optionSeq) => handleSelect(index, optionSeq),
  };
};

const QuizContainer = ({ quizData, contentSeq }: QuizContainerProps) => {
  // 이미 푼 문제인지 확인
  const isAlreadySubmitted = useMemo(() => {
    // submission이 있고, 모든 문제에 userAnswer가 있는 경우에만 true
    return (
      quizData.submission !== null &&
      quizData.questions.every(
        (q) => q.userAnswer !== null && q.userAnswer !== undefined,
      )
    );
  }, [quizData.submission, quizData.questions]);

  // 초기 선택 상태 설정
  const initialSelected = useMemo(() => {
    if (isAlreadySubmitted) {
      // 이미 푼 문제라면 userAnswer에서 선택했던 답을 가져옴
      return quizData.questions.map((q) => {
        if (!q.userAnswer) return null;
        // options 배열에서 선택했던 optionSeq의 인덱스를 찾음
        return q.options.findIndex(
          (opt) => opt.optionSeq === q.userAnswer?.selectedOptionSeq,
        );
      });
    }
    return new Array(quizData.questions.length).fill(null);
  }, [quizData.questions, isAlreadySubmitted]);

  // 퀴즈 답안 상태
  const [selected, setSelected] =
    useState<Array<number | null>>(initialSelected);
  const [showAnswer, setShowAnswer] = useState(isAlreadySubmitted);
  const [quizResult, setQuizResult] = useState<QuizSubmitResponse | null>(
    isAlreadySubmitted && quizData.submission ? {
      submission: {
        seq: quizData.submission.submissionSeq,
        contentSeq: quizData.contentSeq,
        correctAnswers: quizData.submission.correctAnswers,
        totalQuestions: quizData.submission.totalQuestions,
        scorePercentage: quizData.submission.scorePercentage,
        indate: quizData.submission.indate
      },
      questionResults: quizData.questions.map(q => ({
        questionSeq: q.questionSeq,
        question: q.text,
        selectedAnswer: q.userAnswer ? q.userAnswer.selectedOptionSeq.toString() : '',
        correctAnswer: q.userAnswer?.isCorrect ? q.userAnswer.selectedOptionSeq.toString() : '',
        isCorrect: q.userAnswer?.isCorrect || false,
        explanation: ''
      }))
    } : null
  );

  // 정답 선택 처리 함수
  const handleSelect = useCallback((index: number, optionSeq: number) => {
    setSelected((prev) => {
      const newSelected = [...prev];
      newSelected[index] = optionSeq;
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
      answers: quizData.questions.map((question, index) => {
        const selectedOption = question.options[selected[index] || 0];
        return {
          questionSeq: question.questionSeq,
          selectedAnswer: (selectedOption?.optionSeq || 0).toString(),
        };
      }),
    };

    try {
      const response = await submitQuizAnswers(contentSeq, request);
      // API 응답을 통해 문제별 결과 및 설명 업데이트
      setQuizResult(response);
      // 문제별 userAnswer 업데이트
      quizData.questions = quizData.questions.map(q => {
        const result = response.questionResults.find(r => r.questionSeq === q.questionSeq);
        if (result) {
          q.userAnswer = {
            selectedOptionSeq: parseInt(result.selectedAnswer),
            isCorrect: result.isCorrect
          };
        }
        return q;
      });
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
        {quizData.questions.map((question, index) => {
          if (showAnswer) {
            return (
              <QuizAnswer
                key={question.questionSeq}
                {...getAnswerProps(question, index, quizResult?.questionResults)}
              />
            );
          }
          return (
            <QuizCard
              key={question.questionSeq}
              {...getCardProps(question, index, selected, handleSelect)}
            />
          );
        })}
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
