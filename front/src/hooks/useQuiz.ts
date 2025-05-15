import { useState, useMemo, useEffect } from 'react';
import { submitQuizAnswers } from '@/api/quiz';
import {
  QuizData,
  QuizSubmitRequest,
  QuizSubmitResponse,
  QuizAnswerProps,
  QuizCardProps,
  QuizOption,
} from '@/types/quiz';

// 퀴즈 답변 props 생성 유틸리티 함수
const getAnswerProps = (
  question: QuizData['content']['questions'][0],
  index: number,
): QuizAnswerProps => {
  const explanation = question.userAnswer?.explanation || '';

  return {
    questionSeq: index + 1,
    text: question.text,
    correct: question.userAnswer?.isCorrect || false,
    explanation: explanation,
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

// 퀴즈 카드 props 생성 유틸리티 함수
const getCardProps = (
  question: QuizData['content']['questions'][0],
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

export const useQuiz = (quizData: QuizData, contentSeq: string) => {
  // 이미 푼 문제인지 확인
  const isAlreadySubmitted = useMemo(() => {
    if (!quizData?.submission) return false;
    // submission이 있고 모든 문제에 userAnswer가 있는지 확인
    return quizData.content.questions.every((q) => q.userAnswer !== undefined);
  }, [quizData]);

  // 초기 선택 상태 설정
  const initialSelected = useMemo(() => {
    if (!quizData) return [];
    return new Array(quizData.content.questions.length).fill(null);
  }, [quizData]);

  const [selected, setSelected] =
    useState<Array<number | null>>(initialSelected);
  const [showAnswer, setShowAnswer] = useState(isAlreadySubmitted);

  // quizData가 로드되면 showAnswer 상태 업데이트
  useEffect(() => {
    setShowAnswer(isAlreadySubmitted);
  }, [isAlreadySubmitted]);

  const [quizResult, setQuizResult] = useState<QuizSubmitResponse | null>(
    isAlreadySubmitted && quizData?.submission
      ? {
          submission: {
            seq: quizData.submission?.submissionSeq || 0,
            contentSeq: quizData.content.contentSeq,
            correctAnswers: quizData.submission?.correctAnswers || 0,
            totalQuestions: quizData.submission?.totalQuestions || 0,
            scorePercentage: quizData.submission?.scorePercentage || 0,
            indate: quizData.submission?.indate || new Date().toISOString(),
          },
          questionResults: quizData.content.questions.map((q) => ({
            questionSeq: q.questionSeq,
            question: q.text,
            selectedAnswer: q.userAnswer?.selectedOptionSeq
              ? q.userAnswer.selectedOptionSeq.toString()
              : '',
            correctAnswer: q.userAnswer?.isCorrect
              ? q.userAnswer.selectedOptionSeq.toString()
              : '',
            isCorrect: q.userAnswer?.isCorrect || false,
            explanation: q.userAnswer?.explanation || '',
          })),
        }
      : null,
  );

  // 답변이 하나라도 선택되었는지 확인
  const hasAnswered = selected.some((s) => s !== null);

  // 모든 문제에 답변을 선택했는지 여부
  const isCompleted =
    selected.length === quizData?.content.questions.length &&
    selected.every((s) => s !== null);

  // 답변 선택 처리
  const handleSelect = (index: number, optionSeq: number) => {
    if (showAnswer) return;
    setSelected((prev) => {
      const newSelected = [...prev];
      newSelected[index] = optionSeq;
      return newSelected;
    });
  };

  // 퀴즈 제출 처리
  const handleSubmit = async () => {
    if (!isCompleted) return;

    const request: QuizSubmitRequest = {
      answers: quizData.content.questions.map((question, index) => {
        const selectedOptionIndex = selected[index];
        if (selectedOptionIndex === null) {
          throw new Error('모든 문제에 답변해주세요.');
        }
        const selectedOption = question.options.find(
          (opt) => opt.optionSeq === selectedOptionIndex,
        );
        if (!selectedOption) {
          throw new Error('선택한 답변을 찾을 수 없습니다.');
        }
        return {
          questionSeq: question.questionSeq,
          selectedAnswer: selectedOption.text,
        };
      }),
    };

    try {
      const response = await submitQuizAnswers(contentSeq, request);
      if (response.submission && response.questionResults) {
        setQuizResult(response);
        setShowAnswer(true);
        quizData.content.questions = quizData.content.questions.map((q) => {
          const result = response.questionResults.find(
            (r) => r.questionSeq === q.questionSeq,
          );
          if (result) {
            return {
              ...q,
              userAnswer: {
                selectedOptionSeq: parseInt(result.selectedAnswer),
                isCorrect: result.isCorrect,
                explanation: result.explanation,
              },
            };
          }
          return q;
        });
        quizData.submission = response.submission;
        window.scrollTo(0, 0);
      } else {
        throw new Error('퀴즈 결과를 받아올 수 없습니다.');
      }
    } catch (error) {
      console.error('Error submitting quiz:', error);
    }
  };

  return {
    selected,
    showAnswer,
    quizResult,
    isCompleted,
    hasAnswered,
    handleSelect,
    handleSubmit,
    getAnswerProps,
    getCardProps,
  };
};
